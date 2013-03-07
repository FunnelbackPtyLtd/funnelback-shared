package com.funnelback.publicui.test.search.web.controllers.cache.xml;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import lombok.SneakyThrows;

import org.junit.Ignore;

import com.funnelback.common.io.store.Store.RecordAndMetadata;
import com.funnelback.common.io.store.XmlRecord;
import com.funnelback.common.io.store.xml.WarcXmlStore;

@Ignore
public class DatabaseWarcXmlStoreTest extends AbstractXmlCacheControllerTest {

    private static final String COLLECTION_ID = "cache-database-warcxmlstore";
    
    @Override
    protected void storeContent(RecordAndMetadata<XmlRecord> rmd)
            throws IOException {
        WarcXmlStore store = new WarcXmlStore(liveRoot, true);
        store.open();
        store.add(rmd.record);
        store.close();
    }

    @Override
    protected String getPrimaryKey() {
        return "12345";
    }

    @Override
    protected String getCollectionId() {
        return COLLECTION_ID;
    }

    @Override
    @SneakyThrows(UnsupportedEncodingException.class)
    protected String getCacheUrl(String primaryKey) {
        return "local://serve-db-document.tcgi?collection="+COLLECTION_ID+"&record_id="+URLEncoder.encode(getPrimaryKey(), "UTF-8");
    }

}
