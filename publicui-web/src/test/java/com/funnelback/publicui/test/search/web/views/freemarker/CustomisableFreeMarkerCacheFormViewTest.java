package com.funnelback.publicui.test.search.web.views.freemarker;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.config.configtypes.mix.ProfileAndCollectionConfigOption;
import com.funnelback.config.configtypes.service.DefaultServiceConfig;
import com.funnelback.config.configtypes.service.ServiceConfig;
import com.funnelback.config.data.InMemoryConfigData;
import com.funnelback.config.data.environment.NoConfigEnvironment;
import static com.funnelback.config.keys.Keys.FrontEndKeys;
import com.funnelback.config.marshallers.Marshallers;
import com.funnelback.config.validators.Validators;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Profile;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.web.views.freemarker.CustomisableFreeMarkerFormView;
import com.google.common.collect.Maps;

public class CustomisableFreeMarkerCacheFormViewTest extends CustomisableFreeMarkerFormView {

    private static final String PROFILE_NAME = "profileName";
    private Map<String, Object> model;
    private ServiceConfig serviceConfig;
    private MockHttpServletResponse response;
    
    @Before
    public void before() throws Exception {
        model = new HashMap<String, Object>();
        
        serviceConfig = new DefaultServiceConfig(new InMemoryConfigData(Maps.newHashMap()), new NoConfigEnvironment());
        
        Profile p = new Profile();
        p.setServiceConfig(serviceConfig);
        
        Collection c = new Collection("dummy", null);
        c.getProfiles().put(PROFILE_NAME, p);
        model.put(RequestParameters.COLLECTION, c);
        model.put(RequestParameters.PROFILE, PROFILE_NAME);
        
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
        serviceConfig.set(FrontEndKeys.ModernUi.Cache.getCustomContentTypeOptionForForm("simple"), Optional.of("test/junit"));
        customiseOutput("conf/dummy/_default/simple.cache.ftl", model, response);

        Assert.assertEquals("test/junit", response.getContentType());
        Assert.assertEquals(1, response.getHeaderNames().size());
        Assert.assertEquals("Content-Type", response.getHeaderNames().iterator().next());
    }
    
    @Test
    public void testCustomHeaders() {
        serviceConfig.set(new ProfileAndCollectionConfigOption<String>("ui.modern.cache.form.simple.headers.1", Marshallers.STRING_MARSHALLER,
            Validators.acceptAll(), ""), "First-Header: Value 1   ");
        serviceConfig.set(new ProfileAndCollectionConfigOption<String>("ui.modern.cache.form.simple.headers.2", Marshallers.STRING_MARSHALLER,
            Validators.acceptAll(), ""), "Second-Header     :second value...");
        
        customiseOutput("conf/dummy/_default/simple.cache.ftl", model, response);

        Assert.assertEquals("text/html", response.getContentType());
        Assert.assertEquals(3, response.getHeaderNames().size());
        Assert.assertEquals("text/html", response.getHeader("Content-Type"));
        Assert.assertEquals("Value 1", response.getHeader("First-Header"));
        Assert.assertEquals("second value...", response.getHeader("Second-Header"));
    }

    @Test
    public void testBoth() {
        serviceConfig.set(FrontEndKeys.ModernUi.Cache.getCustomContentTypeOptionForForm("simple"), Optional.of("text/csv"));
        serviceConfig.set(new ProfileAndCollectionConfigOption<String>("ui.modern.cache.form.simple.headers.1", Marshallers.STRING_MARSHALLER,
            Validators.acceptAll(), ""), "Content-Disposition: attachment");

        customiseOutput("conf/dummy/_default/simple.cache.ftl", model, response);

        Assert.assertEquals("text/csv", response.getContentType());
        Assert.assertEquals(2, response.getHeaderNames().size());
        Assert.assertEquals("text/csv", response.getHeader("Content-Type"));
        Assert.assertEquals("attachment", response.getHeader("Content-Disposition"));
        
    }

    
}
