package com.funnelback.publicui.test.search.web.filters;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.internal.verification.VerificationModeFactory;

import com.funnelback.common.testutils.SearchHomeConfigs;
import com.funnelback.common.testutils.SearchHomeProvider;
import com.funnelback.publicui.search.web.filters.GroovyFilter;
import com.funnelback.publicui.search.web.filters.GroovyServletFilterHook;
import com.funnelback.publicui.search.web.filters.utils.FilterParameterHandling;
import com.funnelback.springmvc.service.resource.ResourceManager;
import com.google.common.io.Files;

import static org.mockito.Mockito.*;

public class GroovyFilterTests {

    @Test
    public void testFilter() throws IOException, ServletException {
        try {
            File searchHome = SearchHomeProvider.getNamedWritableSearchHomeForTestClass(GroovyFilterTests.class,
                SearchHomeConfigs.getWithDefaults(), "testFilterAbsent");
            
            File collectionConfigDir = new File(searchHome, "conf" + File.separator + "test-collection");
            collectionConfigDir.mkdirs();
            File groovyClass = new File(collectionConfigDir, GroovyFilter.OUTPUT_FILTER_CLASS_FILE_NAME);
            Files.write(
                "public class GroovyServletFilterHookPublicUIImpl extends com.funnelback.publicui.search.web.filters.GroovyServletFilterHook {}",
                groovyClass, StandardCharsets.UTF_8);
            
            GroovyFilter gf = new GroovyFilter();
            gf.setSearchHome(searchHome);
            
            FilterParameterHandling mockFilterParameterHandling = mock(FilterParameterHandling.class);
            when(mockFilterParameterHandling.getCollectionId(any())).thenReturn("test-collection");
            gf.setFilterParameterHandling(mockFilterParameterHandling);
            
            Class<GroovyServletFilterHookCounter> groovyServletFilterHookCounterClass = GroovyServletFilterHookCounter.class;
            ResourceManager mockResourceManager = mock(ResourceManager.class);
            gf.setResourceManager(mockResourceManager);
            when(mockResourceManager.load(any())).thenReturn(groovyServletFilterHookCounterClass);
            
            ServletRequest mockRequest = mock(HttpServletRequest.class);
            ServletResponse mockResponse = mock(ServletResponse.class);
            FilterChain mockFilterChain = mock(FilterChain.class);
            
            gf.doFilter(mockRequest, mockResponse, mockFilterChain);
            
            Assert.assertTrue(GroovyServletFilterHookCounter.preFilterCount == 1);
            verify(mockFilterChain, times(1)).doFilter(any(), any());
            Assert.assertTrue(GroovyServletFilterHookCounter.postFilterCount == 1);
        } finally {
            GroovyServletFilterHookCounter.preFilterCount = 0;
            GroovyServletFilterHookCounter.postFilterCount = 0;
        }
    }
    
    public static class GroovyServletFilterHookCounter extends GroovyServletFilterHook {
        // I'm not keen on these being static (meaning the tests must reset them afterward
        // but since we need to be able to count, but don't have control of the instance created,
        // this sems the simplest approach.
        
        public static int preFilterCount = 0;
        public static int postFilterCount = 0;
        
        public ServletResponse preFilterResponse(ServletRequest request, ServletResponse response) {
            preFilterCount++;
            return response;
        }

        public void postFilterResponse(ServletRequest request, ServletResponse response) {
            postFilterCount++;
        }

    }


    @Test
    public void testFilterAbsent() throws IOException, ServletException {
        File searchHome = SearchHomeProvider.getNamedWritableSearchHomeForTestClass(GroovyFilterTests.class,
            SearchHomeConfigs.getWithDefaults(), "testFilterAbsent");

        // We don't make a groovy file there

        GroovyFilter gf = new GroovyFilter();
        gf.setSearchHome(searchHome);

        FilterParameterHandling mockFilterParameterHandling = mock(FilterParameterHandling.class);
        when(mockFilterParameterHandling.getCollectionId(any())).thenReturn("test-collection");

        ResourceManager mockResourceManager = mock(ResourceManager.class);
        gf.setResourceManager(mockResourceManager);
        verify(mockResourceManager, never()).load(any()); // Never called, because there's no file to load

        ServletRequest mockRequest = mock(HttpServletRequest.class);
        ServletResponse mockResponse = mock(ServletResponse.class);
        FilterChain mockFilterChain = mock(FilterChain.class);

        gf.doFilter(mockRequest, mockResponse, mockFilterChain);

        verify(mockFilterChain, times(1)).doFilter(any(), any());
    }
}

