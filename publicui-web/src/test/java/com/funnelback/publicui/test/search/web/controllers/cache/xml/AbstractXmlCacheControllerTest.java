package com.funnelback.publicui.test.search.web.controllers.cache.xml;

import java.io.File;
import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
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
import com.funnelback.common.config.Config;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.common.io.store.Store.RecordAndMetadata;
import com.funnelback.common.io.store.XmlRecord;
import com.funnelback.common.utils.XMLUtils;
import com.funnelback.publicui.search.model.transaction.cache.CacheQuestion;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.DataRepository;
import com.funnelback.publicui.search.web.controllers.CacheController;
import com.funnelback.publicui.utils.web.MetricsConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public abstract class AbstractXmlCacheControllerTest {

    protected static final File TEST_DOCUMENT = new File("src/test/resources/cache-controller/sample.xml");
    
    protected CacheController cacheController;
    
    @Resource(name="localConfigRepository")
    protected ConfigRepository configRepository;
    
    @Resource(name="localDataRepository")
    protected DataRepository dataRepository;
    
    private MetricRegistry metrics;
    
    protected MockHttpServletRequest request;
    protected MockHttpServletResponse response;
    
    protected String collectionId;
    protected RecordAndMetadata<XmlRecord> rmd;
    protected String cacheUrl;
    protected File liveRoot;
    
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
        
        collectionId = getCollectionId();
        rmd = buildRecordAndMetadata();
        cacheUrl = getCacheUrl(rmd.record.getPrimaryKey());
        
        liveRoot = new File("src/test/resources/dummy-search_home/data/"+getCollectionId()+"/live/data");
        cleanupStore();
        storeContent(rmd);

        // Make sure all conditions are met for cache to be enabled
        Config config = configRepository.getCollection(collectionId).getConfiguration();
        config.setValue(Keys.UI_CACHE_DISABLED, "false");
        config.setValue(Keys.SecurityEarlyBinding.USER_TO_KEY_MAPPER, null);
        config.setValue(Keys.DocumentLevelSecurity.DOCUMENT_LEVEL_SECURITY_MODE, null);
    }


    @Test
    public void testUnknownRecord() throws Exception {
        ModelAndView mav = cacheController.cache(request,
                response,
                new CacheQuestion(
                        configRepository.getCollection(collectionId),
                        DefaultValues.PREVIEW_SUFFIX,
                        DefaultValues.DEFAULT_FORM,
                        "unknown-record", null, 0, -1));
        Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
        Assert.assertEquals(CacheController.CACHED_COPY_UNAVAILABLE_VIEW, mav.getViewName());
        Assert.assertEquals(
            0,
            metrics.counter(
                MetricRegistry.name(MetricsConfiguration.COLLECTION_NS, getCollectionId(),
                    DefaultValues.PREVIEW_SUFFIX, MetricsConfiguration.CACHE)).getCount());
        Assert.assertEquals(
            0,
            metrics.counter(
                MetricRegistry.name(MetricsConfiguration.ALL_NS, MetricsConfiguration.CACHE)).getCount());
    }
    
    @Test
    public void test() throws Exception {
        cacheController.cache(request,
                response,
                new CacheQuestion(
                        configRepository.getCollection(collectionId),
                        DefaultValues.PREVIEW_SUFFIX,
                        DefaultValues.DEFAULT_FORM,
                        cacheUrl.toString(), null, 0, -1));
        
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        Assert.assertEquals("text/xml", response.getContentType());
        Assert.assertEquals(XMLUtils.toString(rmd.record.getContent()), response.getContentAsString());
        Assert.assertEquals(
            1,
            metrics.counter(
                MetricRegistry.name(MetricsConfiguration.COLLECTION_NS, getCollectionId(),
                    DefaultValues.PREVIEW_SUFFIX, MetricsConfiguration.CACHE)).getCount());
        Assert.assertEquals(
            1,
            metrics.counter(
                MetricRegistry.name(MetricsConfiguration.ALL_NS, MetricsConfiguration.CACHE)).getCount());
    }

    /**
     * Generates the test {@link RecordAndMetadata} that will be stored and should be
     * retrieved by the cache controller
     * @return
     */
    protected RecordAndMetadata<XmlRecord> buildRecordAndMetadata() throws IOException {
        return new RecordAndMetadata<XmlRecord>(new XmlRecord(
                XMLUtils.fromFile(TEST_DOCUMENT),
                getPrimaryKey()),
            null);
    }
    
    /**
     * Cleans up the store prior to the test to start from fresh on each test
     * @throws IOException
     */
    protected void cleanupStore() throws IOException {
        File storeRoot = new File("src/test/resources/dummy-search_home/data/"+getCollectionId()+"/live/data");
        FileUtils.deleteDirectory(storeRoot.getParentFile());
        storeRoot.mkdirs();
    }
        
    /**
     * Store the record and metadata into the collection store
     * @param rmd Record and Metadata to store
     * @throws IOException
     */
    protected abstract void storeContent(RecordAndMetadata<XmlRecord> rmd) throws IOException;
    
    /**
     * @return Primary key for the test record
     */
    protected abstract String getPrimaryKey();
    
    /**
     * @return Collection ID to use for the test (Must exist in dummy-search_home)
     */
    protected abstract String getCollectionId();
    
    /**
     * Builds a cache URL from a store record. This is needed because some collection
     * types like Database uses a primary key (<tt>12345</tt>) different from the cache URL returned
     * by PADRE (<tt>local://serve-db-document.cgi?collection=abc&amp;record_id=12345</tt>).
     * 
     * @param primaryKey Primary key of the store record we expect to get
     * @return Cached copy URL as returned by PADRE for a result
     */
    protected abstract String getCacheUrl(String primaryKey);
    
}
