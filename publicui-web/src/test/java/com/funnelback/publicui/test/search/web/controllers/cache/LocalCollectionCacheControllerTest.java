package com.funnelback.publicui.test.search.web.controllers.cache;

import java.io.File;
import java.io.IOException;

import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.servlet.ModelAndView;

import com.codahale.metrics.MetricRegistry;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.publicui.search.model.transaction.cache.CacheQuestion;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.DataRepository;
import com.funnelback.publicui.search.service.security.DLSEnabledCheck;
import com.funnelback.publicui.search.web.controllers.CacheController;
import com.funnelback.publicui.utils.web.MetricsConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class LocalCollectionCacheControllerTest {

    protected CacheController cacheController;
    
    @Resource(name="localConfigRepository")
    protected ConfigRepository configRepository;
    
    @Resource(name="localDataRepository")
    protected DataRepository dataRepository;

    protected MetricRegistry metrics;
    protected MockHttpServletRequest request;
    protected MockHttpServletResponse response;

    @Before
    public void before() throws IOException {
        metrics = new MetricRegistry();
        
        cacheController = new CacheController();
        cacheController.setConfigRepository(configRepository);
        cacheController.setDataRepository(dataRepository);
        cacheController.setMetrics(metrics);
        cacheController.setDLSEnabledCheck(Mockito.mock(DLSEnabledCheck.class));
        
        request = new MockHttpServletRequest();
        request.setRequestURI("/s/cache.html");
        response = new MockHttpServletResponse();
    }
    
    @Test
    public void test() throws Exception {
        ModelAndView mav = cacheController.cache(request,
                response,
                new CacheQuestion(
                        configRepository.getCollection("cache-local"),
                        DefaultValues.PREVIEW_SUFFIX,
                        DefaultValues.DEFAULT_FORM,
                        "unknown-record", "local-collection-cached-document.txt", 0, -1));

        Assert.assertEquals(200, response.getStatus());
        
        Assert.assertEquals(
            Jsoup.parse(FileUtils.readFileToString(new File("src/test/resources/dummy-search_home/share/local-collection-cached-document.txt"))).html(),
            ((Document) mav.getModel().get(CacheController.MODEL_DOCUMENT)).html());

        Assert.assertEquals(
            1,
            metrics.counter(
                MetricRegistry.name(MetricsConfiguration.COLLECTION_NS, "cache-local",
                    DefaultValues.PREVIEW_SUFFIX, MetricsConfiguration.CACHE)).getCount());
        Assert.assertEquals(
            1,
            metrics.counter(
                MetricRegistry.name(MetricsConfiguration.ALL_NS, MetricsConfiguration.CACHE)).getCount());

    }
    
    @Test
    public void testPathOutsideDataRoot() throws Exception {
        cacheController.cache(request,
            response,
            new CacheQuestion(
                    configRepository.getCollection("cache-local"),
                    DefaultValues.PREVIEW_SUFFIX,
                    DefaultValues.DEFAULT_FORM,
                    "unknown-record", "src/test/resources/dummy-search_home/global.cfg.default", 0, -1));
        
        Assert.assertEquals(404, response.getStatus());

        Assert.assertEquals(
            0,
            metrics.counter(
                MetricRegistry.name(MetricsConfiguration.COLLECTION_NS, "cache-local",
                    DefaultValues.PREVIEW_SUFFIX, MetricsConfiguration.CACHE)).getCount());
        Assert.assertEquals(
            0,
            metrics.counter(
                MetricRegistry.name(MetricsConfiguration.ALL_NS, MetricsConfiguration.CACHE)).getCount());
    }
}
