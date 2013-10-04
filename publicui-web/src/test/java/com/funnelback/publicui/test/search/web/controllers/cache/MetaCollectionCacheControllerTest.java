package com.funnelback.publicui.test.search.web.controllers.cache;

import java.io.IOException;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.codahale.metrics.MetricRegistry;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.DataRepository;
import com.funnelback.publicui.search.web.controllers.CacheController;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class MetaCollectionCacheControllerTest {

    protected CacheController cacheController;
    
    @Resource(name="localConfigRepository")
    protected ConfigRepository configRepository;
    
    @Resource(name="localDataRepository")
    protected DataRepository dataRepository;
    
    protected MockHttpServletRequest request;
    protected MockHttpServletResponse response;

    @Before
    public void before() throws IOException {
        cacheController = new CacheController();
        cacheController.setConfigRepository(configRepository);
        cacheController.setDataRepository(dataRepository);
        cacheController.setMetricRegistry(new MetricRegistry());
        
        request = new MockHttpServletRequest();
        request.setRequestURI("/s/cache.html");
        response = new MockHttpServletResponse();
    }
    
    @Test
    public void test() throws Exception {
        cacheController.cache(request,
                response,
                configRepository.getCollection("cache-meta"),
                DefaultValues.PREVIEW_SUFFIX,
                DefaultValues.DEFAULT_FORM,
                "unknown-record", null, 0, -1);
        
        Assert.assertEquals(404, response.getStatus());
    }
}
