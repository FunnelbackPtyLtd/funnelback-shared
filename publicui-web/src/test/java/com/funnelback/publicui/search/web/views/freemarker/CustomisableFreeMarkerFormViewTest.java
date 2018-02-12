package com.funnelback.publicui.search.web.views.freemarker;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import com.funnelback.config.configtypes.service.ServiceConfig;
import com.funnelback.config.data.service.InMemoryServiceConfig;
import com.google.common.collect.ImmutableMap;
public class CustomisableFreeMarkerFormViewTest {

    @Test
    public void testNoValueHeader() {
        ServiceConfig serviceConfig = new InMemoryServiceConfig(
            ImmutableMap.of("ui.modern.form.simple.headers.1", "X-Frame-Options:"));
        
        HttpServletResponse response = mock(HttpServletResponse.class);
        
        new CustomisableFreeMarkerFormView().setCustomHeaders("ui.modern.form.simple.headers.", serviceConfig, response);
        
        verify(response, times(1)).setHeader("X-Frame-Options", "");
    }
    
    @Test
    public void testRemoveHeader() {
        ServiceConfig serviceConfig = new InMemoryServiceConfig(
            ImmutableMap.of("ui.modern.form.foo.remove-headers", "X-Bar, X-Foo , plop"));
        
        HttpServletResponse response = mock(HttpServletResponse.class);
        
        new CustomisableFreeMarkerFormView().removeHeaders("foo", serviceConfig, response);
        
        verify(response, times(1)).setHeader("X-Bar", null);
        verify(response, times(1)).setHeader("X-Foo", null);
        verify(response, times(1)).setHeader("plop", null);
    }
}
