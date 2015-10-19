package com.funnelback.publicui.test.search.web.filters;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.web.filters.ConfigurableCorsFilter;
import com.funnelback.publicui.test.mock.MockConfigRepository;

public class ConfigurableCorsFilterTests {

    private static final String COLLECTION_ID = "interceptor";

    @Autowired
    private MockConfigRepository configRepository = new MockConfigRepository();

    private Config testCollectionConfig;

    private ConfigurableCorsFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @Before
    public void before() throws FileNotFoundException {
        configRepository.removeAllCollections();
        testCollectionConfig = new NoOptionsConfig(new File("src/test/resources/dummy-search_home"), COLLECTION_ID);
        configRepository.addCollection(new Collection(COLLECTION_ID, testCollectionConfig));

        request = new MockHttpServletRequest();
        request.setParameter(RequestParameters.COLLECTION, COLLECTION_ID);

        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
        
        filter = new ConfigurableCorsFilter();
        filter.setConfigRepository(configRepository);
    }
    
    @Test
    public void testNoHeader() throws Exception {
        filter.doFilter(request, response, filterChain);
        
        Assert.assertNull(response.getHeader("Access-Control-Allow-Origin"));
    }
    
    @Test
    public void testHeader() throws Exception {
        testCollectionConfig.setValue(Keys.ModernUI.CORS_ALLOW_ORIGIN, "*.test.com");
        filter.doFilter(request, response, filterChain);
        
        Assert.assertEquals("*.test.com", response.getHeader("Access-Control-Allow-Origin"));
    }
}
