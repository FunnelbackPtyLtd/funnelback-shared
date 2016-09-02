package com.funnelback.publicui.test.search.web.filters.utils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.web.filters.utils.PublicUiServletFilterRequestParameterValueExtractor;

public class PublicUiServletFilterRequestParameterValueExtractorTest {

    @Test
    public void test() throws Exception {
        PublicUiServletFilterRequestParameterValueExtractor publicUiServletFilterRequestParameterValueExtractor = new PublicUiServletFilterRequestParameterValueExtractor();
        
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getParameter(RequestParameters.COLLECTION)).thenReturn("c");
                
        Optional<String> ret = publicUiServletFilterRequestParameterValueExtractor.getCollectionValue(servletRequest);
        Assert.assertTrue(ret.isPresent());
        Assert.assertEquals("c", ret.get());        
    }

    @Test
    public void testResources() throws Exception {
        PublicUiServletFilterRequestParameterValueExtractor publicUiServletFilterRequestParameterValueExtractor = new PublicUiServletFilterRequestParameterValueExtractor();
        
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getPathInfo()).thenReturn("/resources/c/p/foo.css");
                
        Optional<String> ret = publicUiServletFilterRequestParameterValueExtractor.getCollectionValue(servletRequest);
        Assert.assertTrue(ret.isPresent());
        Assert.assertEquals("c", ret.get());        
    }

    @Test
    public void testAbsent() throws Exception {
        PublicUiServletFilterRequestParameterValueExtractor publicUiServletFilterRequestParameterValueExtractor = new PublicUiServletFilterRequestParameterValueExtractor();
        
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
                
        Optional<String> ret = publicUiServletFilterRequestParameterValueExtractor.getCollectionValue(servletRequest);
        Assert.assertFalse(ret.isPresent());
    }

}
