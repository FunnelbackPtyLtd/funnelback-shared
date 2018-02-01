package com.funnelback.publicui.search.web.views.freemarker;

import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import com.funnelback.config.configtypes.service.ServiceConfig;
import com.funnelback.config.data.service.InMemoryServiceConfig;
import com.google.common.collect.ImmutableMap;
import static org.mockito.Mockito.*;
public class CustomisableFreeMarkerFormViewTest {

    @Test
    public void testNoValueHeader() {
        ServiceConfig serviceConfig = new InMemoryServiceConfig(
            ImmutableMap.of("ui.modern.form.simple.headers.1", "X-Frame-Options:"));
        
        HttpServletResponse response = mock(HttpServletResponse.class);
        
        new CustomisableFreeMarkerFormView().setCustomHeaders("ui.modern.form.simple.headers.", serviceConfig, response);
        
        verify(response, times(1)).setHeader("X-Frame-Options", "");
    }
}
