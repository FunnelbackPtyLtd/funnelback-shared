package com.funnelback.publicui.test.search.web.interceptors;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.web.interceptors.ConfigurableCorsInterceptor;
import com.funnelback.publicui.test.mock.MockConfigRepository;

public class ConfigurableCorsInterceptorTests {

    private static final String COLLECTION_ID = "interceptor";

    @Autowired
    private MockConfigRepository configRepository = new MockConfigRepository();

    private Config testCollectionConfig;

    private ConfigurableCorsInterceptor interceptor;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @Before
    public void before() throws FileNotFoundException {
        configRepository.removeAllCollections();
        testCollectionConfig = new NoOptionsConfig(new File("src/test/resources/dummy-search_home"), COLLECTION_ID);
        configRepository.addCollection(new Collection(COLLECTION_ID, testCollectionConfig));

        request = new MockHttpServletRequest();
        request.setParameter(RequestParameters.COLLECTION, COLLECTION_ID);

        response = new MockHttpServletResponse();
        
        interceptor = new ConfigurableCorsInterceptor();
        interceptor.setConfigRepository(configRepository);
    }
    
    @Test
    public void testNoHeader() throws Exception {
        interceptor.postHandle(request, response, null, null);
        
        Assert.assertNull(response.getHeader("Access-Control-Allow-Origin"));
    }
    
    @Test
    public void testHeader() throws Exception {
        testCollectionConfig.setValue(Keys.ModernUI.CORS_ALLOW_ORIGIN, "*.test.com");
        interceptor.postHandle(request, response, null, null);
        
        Assert.assertEquals("*.test.com", response.getHeader("Access-Control-Allow-Origin"));
    }
}
