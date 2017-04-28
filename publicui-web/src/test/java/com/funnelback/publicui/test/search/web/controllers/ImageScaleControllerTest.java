package com.funnelback.publicui.test.search.web.controllers;

import java.net.URL;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletResponse;

import com.funnelback.publicui.search.service.image.ImageFetcher;
import com.funnelback.publicui.search.service.image.ImageScalerSettings;
import com.funnelback.publicui.search.web.controllers.ImageScaleController;

public class ImageScaleControllerTest {

    @Test
    public void testErrorReturn() throws Exception {
        ImageFetcher erroringFetcher = Mockito.mock(ImageFetcher.class);
        Mockito.when(erroringFetcher.fetch(Mockito.any())).thenThrow(new RuntimeException("Some error"));
        
        ImageScaleController controller = new ImageScaleController();
        controller.setFetcher(erroringFetcher);

        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.scale(response, new URL("http://example.com/image.jpg"), new ImageScalerSettings());
        
        Assert.assertEquals(400, response.getStatus());
    }
    
}