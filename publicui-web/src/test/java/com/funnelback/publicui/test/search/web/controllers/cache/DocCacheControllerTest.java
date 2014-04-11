package com.funnelback.publicui.test.search.web.controllers.cache;

import java.io.File;
import java.io.IOException;

import javax.annotation.Resource;

import com.funnelback.common.utils.XMLUtils;
import org.apache.commons.exec.OS;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.servlet.ModelAndView;

import com.codahale.metrics.MetricRegistry;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.DataRepository;
import com.funnelback.publicui.search.web.controllers.CacheController;
import com.funnelback.publicui.utils.web.MetricsConfiguration;


@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class DocCacheControllerTest {

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
        
        request = new MockHttpServletRequest();
        request.setRequestURI("/s/cache.html");
        response = new MockHttpServletResponse();
    }
    
    @Test
    public void testHtml() throws Exception {
        ModelAndView mav = cacheController.cache(request,
            response,
            configRepository.getCollection("cache-doc"),
            DefaultValues.PREVIEW_SUFFIX,
            DefaultValues.DEFAULT_FORM,
            "unknown-record", new File("folder/document.html"), 0, -1);
        
        Assert.assertEquals(200, response.getStatus());
        
        Assert.assertEquals(
            Jsoup.parse(FileUtils.readFileToString(new File("src/test/resources/dummy-search_home/data/cache-doc/live/data/folder/document.html"))).html(),
            ((Document) mav.getModel().get(CacheController.MODEL_DOCUMENT)).html());

        Assert.assertEquals(
            1,
            metrics.counter(
                MetricRegistry.name(MetricsConfiguration.COLLECTION_NS, "cache-doc",
                    DefaultValues.PREVIEW_SUFFIX, MetricsConfiguration.CACHE)).getCount());
        Assert.assertEquals(
            1,
            metrics.counter(
                MetricRegistry.name(MetricsConfiguration.ALL_NS, MetricsConfiguration.CACHE)).getCount());
    }
    
    @Test
    public void testXml() throws Exception {
        cacheController.cache(request,
            response,
            configRepository.getCollection("cache-doc"),
            DefaultValues.PREVIEW_SUFFIX,
            DefaultValues.DEFAULT_FORM,
            "unknown-record", new File("folder/document.xml"), 0, -1);
        
        Assert.assertEquals(200, response.getStatus());
        
        Assert.assertEquals(
            XMLUtils.fromFile(new File("src/test/resources/dummy-search_home/data/cache-doc/live/data/folder/document.xml")).toString(),
            XMLUtils.fromString(response.getContentAsString()).toString());

        Assert.assertEquals(
            1,
            metrics.counter(
                MetricRegistry.name(MetricsConfiguration.COLLECTION_NS, "cache-doc",
                    DefaultValues.PREVIEW_SUFFIX, MetricsConfiguration.CACHE)).getCount());
        Assert.assertEquals(
            1,
            metrics.counter(
                MetricRegistry.name(MetricsConfiguration.ALL_NS, MetricsConfiguration.CACHE)).getCount());
    }
    
    @Test
    public void testNoFile() throws Exception {
        cacheController.cache(request,
            response,
            configRepository.getCollection("cache-doc"),
            DefaultValues.PREVIEW_SUFFIX,
            DefaultValues.DEFAULT_FORM,
            "unknown-record", new File("non-existent/document.html"), 0, -1);

        Assert.assertEquals(404, response.getStatus());

        Assert.assertEquals(
            0,
            metrics.counter(
                MetricRegistry.name(MetricsConfiguration.COLLECTION_NS, "cache-doc",
                    DefaultValues.PREVIEW_SUFFIX, MetricsConfiguration.CACHE)).getCount());
        Assert.assertEquals(
            0,
            metrics.counter(
                MetricRegistry.name(MetricsConfiguration.ALL_NS, MetricsConfiguration.CACHE)).getCount());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testParentFile() throws Exception {
        cacheController.cache(request,
            response,
            configRepository.getCollection("cache-doc"),
            DefaultValues.PREVIEW_SUFFIX,
            DefaultValues.DEFAULT_FORM,
            "unknown-record", new File("../../etc/passwd"), 0, -1);
    }


    @Test(expected=IllegalArgumentException.class)
    public void testAbsoluteFile() throws Exception {
        File f = new File("/etc/passwd");
        if (OS.isFamilyWindows()) {
            f = new File("C:\\Windows\\System32\\cmd.exe");
        }
        
        cacheController.cache(request,
            response,
            configRepository.getCollection("cache-doc"),
            DefaultValues.PREVIEW_SUFFIX,
            DefaultValues.DEFAULT_FORM,
            "unknown-record", f, 0, -1);
    }
}
