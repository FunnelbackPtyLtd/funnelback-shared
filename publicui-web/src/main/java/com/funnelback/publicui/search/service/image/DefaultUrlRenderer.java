package com.funnelback.publicui.search.service.image;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Log4j
@Component
public class DefaultUrlRenderer implements UrlRenderer {
	
	protected static final String CACHE = DefaultUrlRenderer.class.getSimpleName() + "Repository";

	@Autowired
	protected CacheManager appCacheManager;

	@Autowired
	private File searchHome;

	@Override
	public byte[] renderUrl(String url, int width, int height)
			throws IOException {
		Cache cache = appCacheManager.getCache(CACHE);

		String key = url + "|" + width + "|" + height;
		
		if (! cache.isKeyInCache(key)) {
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

			File tempFile = null;
			try {
				tempFile = File.createTempFile("phantom_preview", ".png");
				
				CommandLine cmdLine = new CommandLine(phantomBinary.getAbsolutePath());
				cmdLine.addArgument(phantomPreview.getAbsolutePath());
				cmdLine.addArgument(url);
				cmdLine.addArgument(Integer.toString(width));
				cmdLine.addArgument(Integer.toString(height));
				cmdLine.addArgument(tempFile.getCanonicalPath());
				DefaultExecutor executor = new DefaultExecutor();
	
				int result = executor.execute(cmdLine);
				if (result != 0) {
					throw new RuntimeException(cmdLine.toString() + " failed with code " + result);
				}
		
				byte[] imageBytes = FileUtils.readFileToByteArray(tempFile);

				cache.put(new Element(key, imageBytes));
			} finally {
				if (tempFile != null && tempFile.exists()) {
					tempFile.delete();
				}
			}
			
		}
		
		return (byte[])cache.get(key).getValue();
	}
}
