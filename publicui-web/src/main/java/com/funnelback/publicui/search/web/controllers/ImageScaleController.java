package com.funnelback.publicui.search.web.controllers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.publicui.search.service.image.ImageFetcher;
import com.funnelback.publicui.search.service.image.ImageScaler;
import com.funnelback.publicui.search.service.image.ImageScalerSettings;

import lombok.Cleanup;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * Scale, crop and convert images automatically so they can be presented in
 * Funnelback results.
 */
@Controller
@Log4j2
public class ImageScaleController {

    @Autowired
    @Setter
    ImageFetcher fetcher;

    @Autowired
    @Setter
    ImageScaler scaler;

    /**
     * Scale (and possibly crop and convert) the specified image according to
     * the scaler settings, and return the scaled image.
     * 
     * Caching is used to avoid re-scaling or re-fetching the image. The cache
     * must be manually cleared if the source image is changed.
     */
    @RequestMapping(value="/scale", method=RequestMethod.GET)
    public ModelAndView scale(HttpServletResponse response, URL url, @Valid ImageScalerSettings ss)
        throws Exception {
        
        try {
            byte[] unscaledImage = fetcher.fetch(url.toString());
            byte[] scaledImage = scaler.scaleImage(url.toString(), unscaledImage, ss);
            
            String mimeType = ImageIO.getImageWritersByFormatName(ss.getFormat()).next().getOriginatingProvider().getMIMETypes()[0];
            response.addHeader("Content-Type", mimeType);
    
            @Cleanup InputStream processedImageStream = new ByteArrayInputStream(scaledImage);
            
            org.apache.commons.io.IOUtils.copy(processedImageStream, response.getOutputStream());
            
            response.getOutputStream().close();
        } catch (Exception e) {
            response.sendError(400, e.getMessage());
        }
        
        return null;
    }

    /**
     * Clear the cache for the specified image and scaling settings.
     */
    @RequestMapping(value="/scale/clear-cache", method=RequestMethod.GET)
    public ModelAndView clearCache(URL url, @Valid ImageScalerSettings ss)
        throws Exception {
        fetcher.clearCache(url.toString());
        scaler.clearCache(url.toString(), ss);
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
    
    /**
     * Catch exceptions to avoid them bubbling up and being
     * written to stderr
     * @param ex
     */
    @ExceptionHandler(Exception.class)
    public void exceptionHandler(Exception ex) {
        log.error("Unknown error in "+ImageScaleController.class.getName(), ex);
    }
}
