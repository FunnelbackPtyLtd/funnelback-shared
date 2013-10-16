package com.funnelback.publicui.search.web.controllers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import lombok.Cleanup;

import org.apache.commons.io.IOUtils;
import org.jfree.util.Log;
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

    private static final String DEFAULT_PNG = "/1x1.png";
    private static final int DEFAULT_WIDTH = 1024;
    private static final int DEFAULT_HEIGHT = 768;
    
    @Autowired
    private UrlRenderer renderer;

    @Autowired
    private ImageScaler scaler;

    @RequestMapping(value="/preview", method=RequestMethod.GET)
    public ModelAndView preview(
            HttpServletRequest request,
            HttpServletResponse response,
            String url,
            @Valid ImageScalerSettings ss,
            @RequestParam(value = "render_width", required = false) Integer renderWidth,
            @RequestParam(value = "render_height", required = false) Integer renderHeight)
        throws Exception {
        
        if (renderWidth == null) {
            renderWidth = DEFAULT_WIDTH;
        }
        if (renderHeight == null) {
            renderHeight = DEFAULT_HEIGHT;
        }

        byte[] unscaledImage = renderer.renderUrl(url, renderWidth, renderHeight);
        if (unscaledImage.length > 0) {
            byte[] scaledImage = scaler.scaleImage(
                    PreviewController.class.getCanonicalName() + "|" + url,
                    unscaledImage, ss);
            
            @Cleanup InputStream processedImageStream = new ByteArrayInputStream(scaledImage);
            
            IOUtils.copy(processedImageStream, response.getOutputStream());
            
            response.getOutputStream().close();
        } else {
            // Something went wrong
            Log.error("Unable to obtain a preview for '"+url+"'. Please check the application logs");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("image/png");
            IOUtils.copy(getClass().getResourceAsStream(DEFAULT_PNG), response.getOutputStream());
        }
        
        return null;
    }

}
