package com.funnelback.publicui.search.web.controllers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.Cleanup;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.Thumbnails.Builder;
import net.coobird.thumbnailator.geometry.Positions;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Scale, crop and convert images automatically so they can be presented in
 * Funnelback results.
 */
@Controller
public class ImageScaleController {

	protected static final String CACHE = "imageScaleRepository";

	@Autowired
	protected CacheManager appCacheManager;

	@RequestMapping(value="/scale", method=RequestMethod.GET)
	public ModelAndView scale(HttpServletRequest request,
			HttpServletResponse response, String url, Integer width,
			Integer height,
			@RequestParam(required = false) Boolean keepAspectRatio,
			@RequestParam(required = false) String crop,
			@RequestParam(required = false) String outputFormat)
			throws Exception {
		
		Cache cache = appCacheManager.getCache(CACHE);
		
		String rawKey = "raw:" + url;
		String processedKey = "scaled:" + url + "|" + width + "|" + height + "|" + keepAspectRatio+ "|" + crop+ "|" + outputFormat;
		
		
		if (! cache.isKeyInCache(processedKey)) {
			if (! cache.isKeyInCache(rawKey)) {
				URL imageUrl = new URL(url);
				@Cleanup InputStream imageStream = imageUrl.openStream();
				cache.put(new Element(rawKey, IOUtils.toByteArray(imageStream)));
			}
			
			InputStream imageStream = new ByteArrayInputStream((byte[])cache.get(rawKey).getValue());
			Builder<? extends InputStream> b = Thumbnails.of(imageStream);
	
			b = b.size(width, height);
	
			if (outputFormat != null) {
				b = b.outputFormat(outputFormat);
			}		
	
			if (crop != null) {
				b = b.crop(Positions.valueOf(crop.toUpperCase()));
			} else if (keepAspectRatio != null) {
				b = b.keepAspectRatio(keepAspectRatio);
			} else {
				b = b.keepAspectRatio(true);
			}

			@Cleanup ByteArrayOutputStream scaledImageBytes = new ByteArrayOutputStream();
			b.toOutputStream(scaledImageBytes);
			
			cache.put(new Element(processedKey, scaledImageBytes.toByteArray()));
		}
		
		@Cleanup InputStream processedImageStream = new ByteArrayInputStream((byte[])cache.get(processedKey).getValue());
		
		org.apache.commons.io.IOUtils.copy(processedImageStream, response.getOutputStream());
		
		response.getOutputStream().close();
		
		return null;
	}
		
}
