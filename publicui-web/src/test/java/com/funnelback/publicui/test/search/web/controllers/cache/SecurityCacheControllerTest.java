package com.funnelback.publicui.test.search.web.controllers.cache;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.servlet.ModelAndView;

import com.codahale.metrics.MetricRegistry;
import com.funnelback.common.config.Config;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.common.io.store.RawBytesRecord;
import com.funnelback.common.io.store.Store.RecordAndMetadata;
import com.funnelback.publicui.search.model.transaction.cache.CacheQuestion;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.DataRepository;
import com.funnelback.publicui.search.web.controllers.CacheController;
import com.funnelback.publicui.utils.web.MetricsConfiguration;
import com.funnelback.springmvc.service.security.DLSEnabledChecker;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class SecurityCacheControllerTest {

    protected static final File TEST_DOCUMENT = new File("src/test/resources/cache-controller/sample.html");
    protected static final Map<String, String> METADATA = new HashMap<>();
    static {
        METADATA.put("X-Funnelback-Test", "jUnit");
    }
    
    protected CacheController cacheController;
    
    @Resource(name="localConfigRepository")
    protected ConfigRepository configRepository;
    
    @Resource(name="localDataRepository")
    protected DataRepository dataRepository;
    
    private MetricRegistry metrics;
    
    protected MockHttpServletRequest request;
    protected MockHttpServletResponse response;
    
    protected String collectionId;
    protected RecordAndMetadata<RawBytesRecord> rmd;
    
    protected File liveRoot;
    
    private DLSEnabledChecker dlsChecker;
    
    @Before
    public void before() throws IOException {
        metrics = new MetricRegistry();
        
        cacheController = new CacheController();
        cacheController.setConfigRepository(configRepository);
        cacheController.setDataRepository(dataRepository);
        cacheController.setMetrics(metrics);
        dlsChecker = Mockito.mock(DLSEnabledChecker.class);
        cacheController.setDLSEnabledChecker(dlsChecker);
        
        request = new MockHttpServletRequest();
        request.setRequestURI("/s/cache.html");
        response = new MockHttpServletResponse();
        
        collectionId = "dummy";
        
        
        liveRoot = new File("src/test/resources/dummy-search_home/data/dummy/live/data");
        

        // Make sure all conditions are met for cache to be enabled
        Config config = configRepository.getCollection(collectionId).getConfiguration();
        config.setValue(Keys.UI_CACHE_DISABLED, "false");
        config.setValue(Keys.SecurityEarlyBinding.USER_TO_KEY_MAPPER, null);
        config.setValue(Keys.DocumentLevelSecurity.DOCUMENT_LEVEL_SECURITY_MODE, null);
    }


    @Test
    public void testAccessDeniedBecauseDLSIsEnabled() throws Exception {
        Mockito.when(dlsChecker.isDLSEnabled(Matchers.any())).thenReturn(true);
        ModelAndView mav = cacheController.cache(request,
                response,
                new CacheQuestion(
                        configRepository.getCollection(collectionId),
                        DefaultValues.PREVIEW_SUFFIX,
                        DefaultValues.DEFAULT_FORM,
                        "http://unknown-record", null, 0, -1));
        Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
        Assert.assertEquals(CacheController.CACHED_COPY_UNAVAILABLE_VIEW, mav.getViewName());
        Assert.assertEquals(
            0,
            metrics.counter(
                MetricRegistry.name(MetricsConfiguration.COLLECTION_NS, "dummy",
                    DefaultValues.PREVIEW_SUFFIX, MetricsConfiguration.CACHE)).getCount());
        Assert.assertEquals(
            0,
            metrics.counter(
                MetricRegistry.name(MetricsConfiguration.ALL_NS, MetricsConfiguration.CACHE)).getCount());
    }
    
    
}
