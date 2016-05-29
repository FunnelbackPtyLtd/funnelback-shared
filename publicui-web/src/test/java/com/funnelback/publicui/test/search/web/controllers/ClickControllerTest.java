package com.funnelback.publicui.test.search.web.controllers;

import java.io.IOException;
import java.net.URI;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.log.ClickLog;
import com.funnelback.publicui.search.service.auth.AuthTokenManager;
import com.funnelback.publicui.search.web.controllers.ClickController;

public class ClickControllerTest {

    @Test
    public void testExceptionStillRedirects() throws IOException {
        AuthTokenManager authTokenManager = Mockito.mock(AuthTokenManager.class);
        Mockito.when(authTokenManager.checkToken(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        
        ClickController controller = new ClickController();
        controller.setAuthTokenManager(authTokenManager);
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        Collection c = new Collection("test", Mockito.mock(Config.class));
        Mockito.when(c.getConfiguration().value(Keys.REQUEST_ID_TO_LOG, DefaultValues.REQUEST_ID_TO_LOG.toString()))
            .thenReturn(DefaultValues.REQUEST_ID_TO_LOG.toString());
        
        // Pass a NULL request to cause a NPE
        controller.redirect(null, response, c, ClickLog.Type.CLICK, 0, null, URI.create("http://redirected.com/"), null, "token", false, null);
        
        Assert.assertEquals("http://redirected.com/", response.getRedirectedUrl());
    }
    
    @Test
    public void testExceptionInTokenDoesNotRedirect() throws IOException {
        AuthTokenManager authTokenManager = Mockito.mock(AuthTokenManager.class);
        Mockito.when(authTokenManager.checkToken(Mockito.any(), Mockito.any(), Mockito.any())).thenThrow(new IllegalStateException("unit-test"));
        
        ClickController controller = new ClickController();
        controller.setAuthTokenManager(authTokenManager);
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        try {
            // We should not redirect if the authentication token caused
            // an error, as it could be used as a way to bypass authentication
            // by crafting an exception-causing token
            controller.redirect(new MockHttpServletRequest(), response, new Collection("test", Mockito.mock(Config.class)), ClickLog.Type.CLICK, 0, null, URI.create("http://redirected.com/"), null, "token", false, null);
            Assert.fail("Exception in token processing should have bubbled up");
        } catch (IllegalStateException e) {
            // Ensure the exception is not thrown by something else
            Assert.assertEquals("unit-test", e.getMessage());
        }
        
        Assert.assertNull(response.getRedirectedUrl());
    }
    
}
