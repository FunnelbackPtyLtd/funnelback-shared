package com.funnelback.publicui.test.search.web.filters.utils;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.web.filters.utils.FilterParameterHandling;


import static org.mockito.Mockito.*;
public class FilterParameterHandlingTests {

    @Test
    public void testCollectionParam() {
        HttpServletRequest mockedRequest = mock(HttpServletRequest.class);
        when(mockedRequest.getParameter(RequestParameters.COLLECTION)).thenReturn("test-collection");
        
        String collectionId = new FilterParameterHandling().getCollectionId(mockedRequest);
        
        Assert.assertEquals("test-collection", collectionId);
    }

    @Test
    public void testPathCollection() {
        HttpServletRequest mockedRequest = mock(HttpServletRequest.class);
        when(mockedRequest.getParameter(RequestParameters.COLLECTION)).thenReturn(null);
        when(mockedRequest.getPathInfo()).thenReturn("/resources/test-collection/test-profile/file.ext");
        
        String collectionId = new FilterParameterHandling().getCollectionId(mockedRequest);
        
        Assert.assertEquals("test-collection", collectionId);
    }

    @Test
    public void testNoMatch() {
        HttpServletRequest mockedRequest = mock(HttpServletRequest.class);
        when(mockedRequest.getParameter(RequestParameters.COLLECTION)).thenReturn(null);
        when(mockedRequest.getPathInfo()).thenReturn("/index.html");
        
        String collectionId = new FilterParameterHandling().getCollectionId(mockedRequest);
        
        Assert.assertEquals(null, collectionId);
    }

}
