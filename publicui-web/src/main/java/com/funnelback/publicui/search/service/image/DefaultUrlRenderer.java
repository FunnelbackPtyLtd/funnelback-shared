package com.funnelback.publicui.search.service.image;

import java.io.File;
import java.io.IOException;

import lombok.extern.log4j.Log4j;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.service.ConfigRepository;

@Log4j
@Component
public class DefaultUrlRenderer implements UrlRenderer {
	
	protected static final String CACHE = DefaultUrlRenderer.class.getSimpleName() + "Repository";
	protected static final String PATTERN_KEY = "default_url_renderer.permitted_url_pattern";

	@Autowired
	protected CacheManager appCacheManager;

	@Autowired
	private File searchHome;

	@Autowired
	private ConfigRepository configRepository;

	@Override
	public byte[] renderUrl(String url, int width, int height)
			throws IOException {
		
		String permittedUrlPattern = configRepository.getGlobalConfiguration().value(PATTERN_KEY);
		if (permittedUrlPattern != null && !permittedUrlPattern.isEmpty()) {
			// Compare the URL to the pattern - Fail if it doesn't match
			if (! url.matches(permittedUrlPattern)) {
				throw new RuntimeException("URL is not permitted according to " + PATTERN_KEY);
			}
		}
		
		Cache cache = appCacheManager.getCache(CACHE);

		String key = url + "|" + width + "|" + height;

		if (! cache.isKeyInCache(key)) {
			log.trace("Rendering " + url + " to cache with key " + key);

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
