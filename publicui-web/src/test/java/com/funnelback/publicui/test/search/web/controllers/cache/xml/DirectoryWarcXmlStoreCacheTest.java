package com.funnelback.publicui.test.search.web.controllers.cache.xml;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import lombok.SneakyThrows;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.io.store.Store.RecordAndMetadata;
import com.funnelback.common.io.store.XmlRecord;
import com.funnelback.common.io.store.xml.WarcXmlStore;

public class DirectoryWarcXmlStoreCacheTest extends
        AbstractXmlCacheControllerTest {

    private static final String COLLECTION_ID = "cache-directory-warcxmlstore";
    
    @Override
    protected void storeContent(RecordAndMetadata<XmlRecord> rmd)
            throws IOException {
        WarcXmlStore store = new WarcXmlStore(liveRoot, DefaultValues.Warc.WARC_COMPRESSION);
        store.open();
        store.add(rmd.record);
        store.close();

    }

    @Override
    protected String getPrimaryKey() {
        return "CN=Guest,CN=Users,DC=harness,DC=local";
    }

    @Override
    protected String getCollectionId() {
        return COLLECTION_ID;
    }

    @Override
    @SneakyThrows(UnsupportedEncodingException.class)
    protected String getCacheUrl(String primaryKey) {
        return "local://serve-directory-document.tcgi?collection=Se2-Directory-Harness&record_id="+URLEncoder.encode(getPrimaryKey(), "UTF-8");
    }

}
