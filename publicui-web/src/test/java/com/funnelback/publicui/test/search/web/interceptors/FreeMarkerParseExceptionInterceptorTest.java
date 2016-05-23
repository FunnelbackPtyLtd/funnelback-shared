package com.funnelback.publicui.test.search.web.interceptors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.web.interceptors.FreeMarkerParseExceptionInterceptor;

import freemarker.core.ParseException;

public class FreeMarkerParseExceptionInterceptorTest {

    private ConfigRepository configRepository;
    private FreeMarkerParseExceptionInterceptor interceptor;
    private MockHttpServletResponse response;
    private MockHttpServletRequest request;
    
    @Before
    public void before() {
        
        configRepository = Mockito.mock(ConfigRepository.class);
        
        interceptor = new FreeMarkerParseExceptionInterceptor();
        interceptor.setConfigRepository(configRepository);
        
        response = new MockHttpServletResponse();
        request = new MockHttpServletRequest();
    }
    
    @Test
    public void testNoException() throws Exception {
        interceptor.afterCompletion(request, response, null, null);
        
        Assert.assertEquals("No content should have been written", "", response.getContentAsString());
    }
    
    @Test
    public void testNoParseException() throws Exception {
        interceptor.afterCompletion(request, response, null, new RuntimeException(new RuntimeException()));

        Assert.assertEquals("No content should have been written", "", response.getContentAsString());
    }
    
    @Test
    public void testParseExceptionNoCollection() throws Exception {
        // No collection in request
        interceptor.afterCompletion(request, response, null, new ParseException("description", null, 0, 0, 0, 0));
        Assert.assertEquals("No content should have been written", "", response.getContentAsString());
        
        // Invalid collection (not in the repository)
        request.setParameter("collection", "collection");
        interceptor.afterCompletion(request, response, null, new ParseException("description", null, 0, 0, 0, 0));
        Assert.assertEquals("No content should have been written", "", response.getContentAsString());
    }
    
    @Test
    public void testParseExceptionCollectionDisplayErrors() throws Exception {
        Config c = Mockito.mock(Config.class);
        Mockito
            .when(c.valueAsBoolean(Keys.ModernUI.FREEMARKER_DISPLAY_ERRORS, DefaultValues.ModernUI.FREEMARKER_DISPLAY_ERRORS))
            .thenReturn(Boolean.TRUE);
        Mockito
            .when(configRepository.getCollection("collection"))
            .thenReturn(new Collection("collection", c));
        
        request.setParameter("collection", "collection");
        interceptor.afterCompletion(request, response, null, new ParseException("description", null, 0, 0, 0, 0));
        
        Assert.assertFalse("Exception message should have been written", "".equals(response.getContentAsString())); 
    }

    @Test
    public void testParseExceptionCollectionNoDisplayErrors() throws Exception {
        Config c = Mockito.mock(Config.class);
        Mockito
            .when(c.valueAsBoolean(Keys.ModernUI.FREEMARKER_DISPLAY_ERRORS, DefaultValues.ModernUI.FREEMARKER_DISPLAY_ERRORS))
            .thenReturn(Boolean.FALSE);
        Mockito
            .when(configRepository.getCollection("collection"))
            .thenReturn(new Collection("collection", c));
        
        request.setParameter("collection", "collection");
        interceptor.afterCompletion(request, response, null, new ParseException("description", null, 0, 0, 0, 0));
        
        Assert.assertEquals("No content should have been written", "", response.getContentAsString()); 
    }
    
    @Test
    public void testNestedParseException() throws Exception {
        Config c = Mockito.mock(Config.class);
        Mockito
            .when(c.valueAsBoolean(Keys.ModernUI.FREEMARKER_DISPLAY_ERRORS, DefaultValues.ModernUI.FREEMARKER_DISPLAY_ERRORS))
            .thenReturn(Boolean.TRUE);
        Mockito
            .when(configRepository.getCollection("collection"))
            .thenReturn(new Collection("collection", c));
        
        request.setParameter("collection", "collection");
        interceptor.afterCompletion(request, response, null, new RuntimeException(
            new RuntimeException(new ParseException("description", null, 0, 0, 0, 0))));
        
        Assert.assertFalse("Exception message should have been written", "".equals(response.getContentAsString())); 
        
    }

}
