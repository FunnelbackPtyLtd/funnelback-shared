package com.funnelback.publicui.test.search.web.controllers.cache.xml;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

import lombok.SneakyThrows;

import com.funnelback.common.Xml;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.io.store.Store.RecordAndMetadata;
import com.funnelback.common.io.store.XmlRecord;
import com.funnelback.publicui.search.web.controllers.CacheController;

public class TRIMPushFolderXmlStoreCacheTest extends
        AbstractXmlCacheControllerTest {

    private static final String COLLECTION_ID = "cache-trimpush-xmlstore";
    
    @Override
    protected void storeContent(RecordAndMetadata<XmlRecord> rmd)
            throws IOException {
        // Noop, data is already present in live/45/...
    }
    
    @Override
    protected void cleanupStore() throws IOException {
        // Noop, data is already present in live/45/..
    }
    
    @Override
    protected RecordAndMetadata<XmlRecord> buildRecordAndMetadata()
            throws IOException {
        return new RecordAndMetadata<XmlRecord>(
                new XmlRecord(Xml.fromFile(
                        new File("src/test/resources/dummy-search_home/data/cache-trimpush-xmlstore/live/data/45/1/1357-Embedded mail level 1 (root).html")),
                        getPrimaryKey()),
                null);
    }

    @Override
    protected String getPrimaryKey() {
        return "trim://45/1357/Embedded mail level 1 (root).html";
    }

    @Override
    protected String getCollectionId() {
        return COLLECTION_ID;
    }

    @Override
    @SneakyThrows(UnsupportedEncodingException.class)
    protected String getCacheUrl(String primaryKey) {
        return "trim://45/1357/"
                +URLEncoder.encode("Embedded mail level 1 (root).html", "UTF-8");
    }

    @Test
    @Override
    public void testUnknownRecord() throws Exception {
        ModelAndView mav = cacheController.cache(request,
                response,
                configRepository.getCollection(collectionId),
                DefaultValues.PREVIEW_SUFFIX,
                DefaultValues.DEFAULT_FORM,
                "trim://55/1234", null, 0, -1);
        Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
        Assert.assertEquals(CacheController.CACHED_COPY_UNAVAILABLE_VIEW, mav.getViewName());
    }

}
