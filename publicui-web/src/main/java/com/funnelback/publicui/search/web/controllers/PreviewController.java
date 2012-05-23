package com.funnelback.publicui.search.web.controllers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.Cleanup;
import net.coobird.thumbnailator.Thumbnails;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * Return a preview image of a given URL.
 */
@Controller
public class PreviewController {
	protected static final String CACHE = "phantomPreviewRepository";

	@Autowired
	protected CacheManager appCacheManager;
	
	@Autowired
	private File searchHome;

	@RequestMapping(value="/preview", method=RequestMethod.GET)
	public ModelAndView preview(HttpServletRequest request,
			HttpServletResponse response,
			String url) throws Exception {
		
		Cache cache = appCacheManager.getCache(CACHE);
		
		String cacheKey = "preview:" + url;		
		
		if (! cache.isKeyInCache(cacheKey)) {
			File phantomBinary;
			
			boolean isWindows = System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;
			boolean isMac = System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0;
			boolean is64Bit = false;
			
			if (isMac) {
				phantomBinary = new File(searchHome, "mbin" + File.separator + "phantomjs" + File.separator + "bin" + File.separator + "phantomjs");
			} else if (isWindows) {
				phantomBinary = new File(searchHome, "wbin" + File.separator + "phantomjs" + File.separator + "phantomjs.exe");
			} else {
				if (is64Bit) {
					phantomBinary = new File(searchHome, "linbin" + File.separator + "phantomjs" + File.separator + "64" + File.separator + "bin" + File.separator + "phantomjs");
				} else {
					phantomBinary = new File(searchHome, "linbin" + File.separator + "phantomjs" + File.separator + "32" + File.separator + "bin" + File.separator + "phantomjs");					
				}
			}

			File phantomPreview = new File(searchHome, "bin" + File.separator + "phantom_preview.js");

			// FIXME - Need to avoid hardcoded temporary file names
			
			CommandLine cmdLine = new CommandLine(phantomBinary.getAbsolutePath());
			cmdLine.addArgument(phantomPreview.getAbsolutePath());
			cmdLine.addArgument(url);
			DefaultExecutor executor = new DefaultExecutor();
			executor.setWorkingDirectory(new File(System.getProperty("java.io.tmpdir")));

			int result = executor.execute(cmdLine);
			
			assert(result == 0);
	
			@Cleanup ByteArrayOutputStream previewBytes = new ByteArrayOutputStream();
	
			Thumbnails.of(new File(System.getProperty("java.io.tmpdir"), "output.png"))
	        .size(400, 300)
			.keepAspectRatio(true)
			.outputFormat("png")
	        .toOutputStream(previewBytes);
			
			cache.put(new Element(cacheKey, previewBytes.toByteArray()));
		}
		
		@Cleanup InputStream processedImageStream = new ByteArrayInputStream((byte[])cache.get(cacheKey).getValue());
		
		org.apache.commons.io.IOUtils.copy(processedImageStream, response.getOutputStream());
		
		response.getOutputStream().close();
		
		return null;
	}

}
