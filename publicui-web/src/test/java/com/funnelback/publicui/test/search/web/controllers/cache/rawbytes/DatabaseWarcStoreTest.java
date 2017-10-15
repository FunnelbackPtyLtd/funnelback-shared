package com.funnelback.publicui.test.search.web.controllers.cache.rawbytes;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.funnelback.common.io.store.RawBytesRecord;
import com.funnelback.common.io.store.Store.RecordAndMetadata;
import com.funnelback.common.io.store.bytes.WarcFileStore;

import lombok.SneakyThrows;

public class DatabaseWarcStoreTest extends AbstractRawBytesCacheControllerTest {

    private static final String COLLECTION_ID = "cache-database-warcxmlstore";
    
    @Override
    @SneakyThrows(UnsupportedEncodingException.class)
    protected String getPrimaryKey() {
        return "local://serve-db-document.tcgi?collection="+COLLECTION_ID+"&record_id="+URLEncoder.encode("12345", "UTF-8");
    }

    @Override
    protected String getCollectionId() {
        return COLLECTION_ID;
    }

    @Override
    protected String getCacheUrl(String primaryKey) {
        return getPrimaryKey();
    }

    @Override
    protected void storeContent(RecordAndMetadata<RawBytesRecord> rmd) throws IOException {
        WarcFileStore store = new WarcFileStore(liveRoot);
        store.open();
        store.add(rmd.record, rmd.metadata);
        store.close();
    }
}
