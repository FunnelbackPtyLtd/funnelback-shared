package com.funnelback.publicui.search.web.controllers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import lombok.Cleanup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

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

    /**
     * Scale (and possibly crop and convert) the specified image according to
     * the scaler settings, and return the scaled image.
     * 
     * Caching is used to avoid re-scaling or re-fetching the image. The cache
     * must be manually cleared if the source image is changed.
     */
    @RequestMapping(value="/scale", method=RequestMethod.GET)
    public ModelAndView scale(HttpServletResponse response, String url, @Valid ImageScalerSettings ss)
        throws Exception {
        
        byte[] unscaledImage = fetcher.fetch(url);
        byte[] scaledImage = scaler.scaleImage(url, unscaledImage, ss);
        
        String mimeType = ImageIO.getImageWritersByFormatName(ss.getFormat()).next().getOriginatingProvider().getMIMETypes()[0];
        response.addHeader("Content-Type", mimeType);

        @Cleanup InputStream processedImageStream = new ByteArrayInputStream(scaledImage);
        
        org.apache.commons.io.IOUtils.copy(processedImageStream, response.getOutputStream());
        
        response.getOutputStream().close();
        
        return null;
    }

    /**
     * Clear the cache for the specified image and scaling settings.
     */
    @RequestMapping(value="/scale/clear-cache", method=RequestMethod.GET)
    public ModelAndView clearCache(String url, @Valid ImageScalerSettings ss)
        throws Exception {
        fetcher.clearCache(url);
        scaler.clearCache(url, ss);
        return null;
    }

    /**
     * Clear all cache entries.
     */
    @RequestMapping(value="/scale/clear-all-cache", method=RequestMethod.GET)
    public ModelAndView clearAllCache()
        throws Exception {
        fetcher.clearAllCache();
        scaler.clearAllCache();
        return null;
    }
}
