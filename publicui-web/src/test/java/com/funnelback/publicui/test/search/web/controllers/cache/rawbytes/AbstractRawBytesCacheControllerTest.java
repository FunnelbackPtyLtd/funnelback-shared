package com.funnelback.publicui.test.search.web.controllers.cache.rawbytes;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

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
import com.funnelback.common.config.Config;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.common.io.store.RawBytesRecord;
import com.funnelback.common.io.store.Store.RecordAndMetadata;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.DataRepository;
import com.funnelback.publicui.search.web.controllers.CacheController;
import com.funnelback.publicui.utils.web.MetricsConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public abstract class AbstractRawBytesCacheControllerTest {

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
    protected String cacheUrl;
    protected File liveRoot;
    
    @Before
    public void before() throws IOException {
        metrics = new MetricRegistry();
        
        cacheController = new CacheController();
        cacheController.setConfigRepository(configRepository);
        cacheController.setDataRepository(dataRepository);
        cacheController.setMetricRegistry(metrics);
        
        request = new MockHttpServletRequest();
        request.setRequestURI("/s/cache.html");
        response = new MockHttpServletResponse();
        
        collectionId = getCollectionId();
        rmd = buildRecordAndMetadata();
        cacheUrl = getCacheUrl(rmd.record.getPrimaryKey());

        metrics.remove( MetricRegistry.name(MetricsConfiguration.COLLECTION_NS, getCollectionId(),
            DefaultValues.DEFAULT_PROFILE, MetricsConfiguration.CACHE));
        
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
                configRepository.getCollection(collectionId),
                DefaultValues.PREVIEW_SUFFIX,
                DefaultValues.DEFAULT_FORM,
                "http://unknown-record", null, 0, -1);
        Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
        Assert.assertEquals(CacheController.CACHED_COPY_UNAVAILABLE_VIEW, mav.getViewName());
        Assert.assertEquals(
            0,
            metrics.counter(
                MetricRegistry.name(MetricsConfiguration.COLLECTION_NS, getCollectionId(),
                    DefaultValues.PREVIEW_SUFFIX, MetricsConfiguration.CACHE)).getCount());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void test() throws Exception {
        ModelAndView mav = cacheController.cache(request,
                response,
                configRepository.getCollection(collectionId),
                DefaultValues.PREVIEW_SUFFIX,
                DefaultValues.DEFAULT_FORM,
                cacheUrl.toString(), null, 0, -1);
        
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());

        Assert.assertEquals(cacheUrl, mav.getModel().get(RequestParameters.Cache.URL));
        Assert.assertEquals(request.getRequestURL().toString(), mav.getModel().get(CacheController.MODEL_REQUEST_URL).toString());

        Assert.assertEquals(
                configRepository.getCollection(collectionId).getId(),
                ((Collection) mav.getModel().get(RequestParameters.COLLECTION)).getId());
        
        Assert.assertEquals(rmd.metadata.get("X-Funnelback-Test"),
                ((Map<String, String>) mav.getModel().get(CacheController.MODEL_METADATA)).get("X-Funnelback-Test"));
        Assert.assertEquals(
                Jsoup.parse(new String(rmd.record.getContent())).html(),
                ((Document) mav.getModel().get(CacheController.MODEL_DOCUMENT)).html());

        Assert.assertEquals(
            1,
            metrics.counter(
                MetricRegistry.name(MetricsConfiguration.COLLECTION_NS, getCollectionId(),
                    DefaultValues.PREVIEW_SUFFIX, MetricsConfiguration.CACHE)).getCount());

    }

    /**
     * Generates the test {@link RecordAndMetadata} that will be stored and should be
     * retrieved by the cache controller
     * @return
     */
    protected RecordAndMetadata<RawBytesRecord> buildRecordAndMetadata() throws IOException {
        return new RecordAndMetadata<RawBytesRecord>(new RawBytesRecord(
                FileUtils.readFileToByteArray(TEST_DOCUMENT),
                getPrimaryKey()),
            METADATA);
    }
    
    /**
     * Cleans up the store prior to the test to start from fresh on each test
     * @throws IOException
     */
    protected void cleanupStore() throws IOException {
        File storeRootLive = new File("src/test/resources/dummy-search_home/data/"+getCollectionId()+"/live");
        File storeRootOffline = new File("src/test/resources/dummy-search_home/data/"+getCollectionId()+"/offline");
        for (File f: new File[] {storeRootLive, storeRootOffline}) {
            FileUtils.deleteDirectory(f);            
            new File(f, "data").mkdirs();
            new File(f, "checkpoint").mkdirs();
        }
    }
        
    /**
     * Store the record and metadata into the collection store
     * @param rmd Record and Metadata to store
     * @throws IOException
     */
    protected abstract void storeContent(RecordAndMetadata<RawBytesRecord> rmd) throws IOException;
    
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
