package com.funnelback.publicui.test.search.web.controllers.cache.rawbytes;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.funnelback.common.io.store.RawBytesRecord;
import com.funnelback.common.io.store.Store.RecordAndMetadata;
import com.funnelback.common.io.store.bytes.FlatFileStore;

import lombok.SneakyThrows;

public class DirectoryFlatFileStoreCacheTest extends
        AbstractRawBytesCacheControllerTest {

    private static final String COLLECTION_ID = "cache-directory-flatfilestore";
    
    @Override
    protected void storeContent(RecordAndMetadata<RawBytesRecord> rmd) throws IOException {
        FlatFileStore store = new FlatFileStore(liveRoot);
        store.open();
        store.add(rmd.record, rmd.metadata);
        store.close();
    }

    @Override
    @SneakyThrows(UnsupportedEncodingException.class)
    protected String getPrimaryKey() {
        return "local://serve-directory-document.tcgi?collection=Se2-Directory-Harness&record_id="
                    +URLEncoder.encode("CN=Guest,CN=Users,DC=harness,DC=local", "UTF-8");
    }

    @Override
    protected String getCollectionId() {
        return COLLECTION_ID;
    }

    @Override
    protected String getCacheUrl(String primaryKey) {
        return primaryKey;
    }

}
