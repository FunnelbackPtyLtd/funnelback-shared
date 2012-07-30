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

import com.funnelback.publicui.search.service.anchors.AnchorsFetcher;
import com.funnelback.publicui.search.service.image.ImageFetcher;
import com.funnelback.publicui.search.service.image.ImageScaler;
import com.funnelback.publicui.search.service.image.ImageScalerSettings;

/**
 * Scale, crop and convert images automatically so they can be presented in
 * Funnelback results.
 */
@Controller
public class ImageScaleController {

	@Autowired
	ImageFetcher fetcher;

	@Autowired
	ImageScaler scaler;

	@RequestMapping(value="/scale", method=RequestMethod.GET)
	public ModelAndView scale(HttpServletRequest request,
			HttpServletResponse response, String url, ImageScalerSettings ss)
			throws Exception {
		
		byte[] unscaledImage = fetcher.fetch(url);
		byte[] scaledImage = scaler.scaleImage(url, unscaledImage, ss);
		
		@Cleanup InputStream processedImageStream = new ByteArrayInputStream(scaledImage);
		
		org.apache.commons.io.IOUtils.copy(processedImageStream, response.getOutputStream());
		
		response.getOutputStream().close();
		
		return null;
	}
		
}
