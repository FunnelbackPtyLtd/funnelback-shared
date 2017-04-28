package com.funnelback.publicui.search.web.filters;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import freemarker.core.ParseException;
import freemarker.core.TemplateObject;
import freemarker.template.TemplateException;


public class UnhandledExceptionFilterTest {

    private UnhandledExceptionFilter filter;
    private FilterChain chain;
    private HttpServletRequest req;
    
    @Before
    public void before() {
        filter = new UnhandledExceptionFilter();
        chain = Mockito.mock(FilterChain.class);
        req = Mockito.mock(HttpServletRequest.class);
    }
    
    @Test
    public void testNoExceptions() throws Exception {
        MockHttpServletResponse res = new MockHttpServletResponse();
        res.setStatus(1234);
        
        filter.doFilter(req, res, chain);
        
        Assert.assertEquals(1234, res.getStatus());
    }
    
    @Test
    public void testNotFreeMarkerException() throws Exception {
        MockHttpServletResponse res = new MockHttpServletResponse();
        res.setStatus(1234);
        Mockito.doThrow(Exception.class).when(chain).doFilter(Mockito.any(), Mockito.any());

        filter.doFilter(req, res, chain);
        
        Assert.assertEquals(500, res.getStatus());
        Mockito.verify(req).getRequestURL();
        Mockito.verify(req).getQueryString();
    }

    @Test
    public void testFreeMarkerException() throws Exception {
        for (Class<Exception> clazz: new Class[] {
            ParseException.class, TemplateException.class
        }) {
            MockHttpServletResponse res = new MockHttpServletResponse();
            res.setStatus(1234);
            HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
            Mockito.doThrow(clazz).when(chain).doFilter(Mockito.any(), Mockito.any());

            filter.doFilter(req, res, chain);
            Assert.assertEquals(500, res.getStatus());
            
            Mockito.verifyZeroInteractions(req);
        }
    }

    @Test
    public void testNestedFreeMarkerParseException() throws Exception {
        MockHttpServletResponse res = new MockHttpServletResponse();
        res.setStatus(1234);
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        
        RuntimeException e = new RuntimeException(new RuntimeException(new RuntimeException(new ParseException("", 0, 0))));
        Mockito.doThrow(e).when(chain).doFilter(Mockito.any(), Mockito.any());

        filter.doFilter(req, res, chain);
        Assert.assertEquals(500, res.getStatus());
        
        Mockito.verifyZeroInteractions(req);
    }

    @Test
    public void testNestedFreeMarkerTemplateException() throws Exception {
        MockHttpServletResponse res = new MockHttpServletResponse();
        res.setStatus(1234);
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        
        RuntimeException e = new RuntimeException(new RuntimeException(new RuntimeException(new TemplateException(null))));
        Mockito.doThrow(e).when(chain).doFilter(Mockito.any(), Mockito.any());

        filter.doFilter(req, res, chain);
        Assert.assertEquals(500, res.getStatus());
        
        Mockito.verifyZeroInteractions(req);
    }

}
