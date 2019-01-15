package com.funnelback.publicui.test.search.web.views.freemarker;

import static com.funnelback.config.keys.Keys.FrontEndKeys;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletResponse;

import com.funnelback.config.configtypes.service.DefaultServiceConfig;
import com.funnelback.config.configtypes.service.ServiceConfig;
import com.funnelback.config.configtypes.service.ServiceConfigOption;
import com.funnelback.config.data.InMemoryConfigData;
import com.funnelback.config.data.environment.NoConfigEnvironment;
import com.funnelback.config.marshallers.Marshallers;
import com.funnelback.config.validators.Validators;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.web.views.freemarker.CustomisableFreeMarkerFormView;
import com.google.common.collect.Maps;

public class CustomisableFreeMarkerSearchFormViewTest extends CustomisableFreeMarkerFormView {

    private Map<String, Object> model;
    private MockHttpServletResponse response;
    private ServiceConfig serviceConfig;
    
    @Before
    public void before() throws Exception {
        File searchHome = new File("src/test/resources/dummy-search_home");
        model = new HashMap<String, Object>();
        
        serviceConfig = new DefaultServiceConfig(new InMemoryConfigData(Maps.newHashMap()), new NoConfigEnvironment());
        
        SearchQuestion mockSearchQuestion = Mockito.mock(SearchQuestion.class);
        Mockito.when(mockSearchQuestion.getCurrentProfileConfig()).thenReturn(serviceConfig);
        
        model.put("question", mockSearchQuestion);
        
        response = new MockHttpServletResponse();
        response.setContentType("text/html");
    }
    
    @Test
    public void testNothingToDo() {
        // No custom content type or header
        customiseOutput("conf/dummy/_default/simple.cache.ftl", model, response);
        
        Assert.assertEquals("text/html", response.getContentType());
        Assert.assertEquals(1, response.getHeaderNames().size());
        Assert.assertEquals("Content-Type", response.getHeaderNames().iterator().next());
    }
    
    @Test
    public void testCustomContentType() {
        serviceConfig.set(FrontEndKeys.ModernUi.getCustomContentTypeOptionForForm("simple"), Optional.of("test/junit"));
        customiseOutput("conf/dummy/_default/simple.ftl", model, response);

        Assert.assertEquals("test/junit", response.getContentType());
        Assert.assertEquals(1, response.getHeaderNames().size());
        Assert.assertEquals("Content-Type", response.getHeaderNames().iterator().next());
    }
    
    @Test
    public void testCustomHeaders() {
        serviceConfig.set(new ServiceConfigOption<String>("ui.modern.form.simple.headers.1", Marshallers.STRING_MARSHALLER,
            Validators.acceptAll(), ""), "First-Header: Value 1   ");
        serviceConfig.set(new ServiceConfigOption<String>("ui.modern.form.simple.headers.2", Marshallers.STRING_MARSHALLER,
            Validators.acceptAll(), ""), "Second-Header     :second value...");
        
        customiseOutput("conf/dummy/_default/simple.ftl", model, response);

        Assert.assertEquals("text/html", response.getContentType());
        Assert.assertEquals(3, response.getHeaderNames().size());
        Assert.assertEquals("text/html", response.getHeader("Content-Type"));
        Assert.assertEquals("Value 1", response.getHeader("First-Header"));
        Assert.assertEquals("second value...", response.getHeader("Second-Header"));
    }
    
    @Test
    public void testCustomHeaderColon() {
        serviceConfig.set(new ServiceConfigOption<String>("ui.modern.form.simple.headers.99", Marshallers.STRING_MARSHALLER,
            Validators.acceptAll(), ""), "Access-Control-Allow-Origin: http://server.com/");
        
        customiseOutput("conf/dummy/_default/simple.ftl", model, response);

        Assert.assertEquals("text/html", response.getContentType());
        Assert.assertEquals(2, response.getHeaderNames().size());
        Assert.assertEquals("http://server.com/", response.getHeader("Access-Control-Allow-Origin"));
    }

    @Test
    public void testBoth() {
        serviceConfig.set(FrontEndKeys.ModernUi.getCustomContentTypeOptionForForm("simple"), Optional.of("text/csv"));
        serviceConfig.set(new ServiceConfigOption<String>("ui.modern.form.simple.headers.1", Marshallers.STRING_MARSHALLER,
            Validators.acceptAll(), ""), "Content-Disposition: attachment");

        customiseOutput("conf/dummy/_default/simple.ftl", model, response);

        Assert.assertEquals("text/csv", response.getContentType());
        Assert.assertEquals(2, response.getHeaderNames().size());
        Assert.assertEquals("text/csv", response.getHeader("Content-Type"));
        Assert.assertEquals("attachment", response.getHeader("Content-Disposition"));
        
    }
    
}
