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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.publicui.search.service.image.ImageScaler;
import com.funnelback.publicui.search.service.image.ImageScalerSettings;
import com.funnelback.publicui.search.service.image.UrlRenderer;

/**
 * Return a preview image of a given URL.
 */
@Controller
public class PreviewController {

	@Autowired
	UrlRenderer renderer;

	@Autowired
	ImageScaler scaler;

	@RequestMapping(value="/preview", method=RequestMethod.GET)
	public ModelAndView preview(
			HttpServletRequest request,
			HttpServletResponse response,
			String url,
			ImageScalerSettings ss,
			@RequestParam(value = "render_width", required = false) Integer renderWidth,
			@RequestParam(value = "render_height", required = false) Integer renderHeight)
			throws Exception {
		
		if (renderWidth == null) {
			renderWidth = 1024;
		}
		if (renderHeight == null) {
			renderHeight = 1024;
		}

		byte[] unscaledImage = renderer.renderUrl(url, renderWidth, renderHeight);
		byte[] scaledImage = scaler.scaleImage(
				PreviewController.class.getCanonicalName() + "|" + url,
				unscaledImage, ss);
		
		@Cleanup InputStream processedImageStream = new ByteArrayInputStream(scaledImage);
		
		org.apache.commons.io.IOUtils.copy(processedImageStream, response.getOutputStream());
		
		response.getOutputStream().close();
		
		return null;
	}

}
