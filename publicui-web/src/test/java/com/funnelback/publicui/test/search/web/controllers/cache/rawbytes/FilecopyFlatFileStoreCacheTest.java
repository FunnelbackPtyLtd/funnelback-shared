package com.funnelback.publicui.test.search.web.controllers.cache.rawbytes;

import java.io.IOException;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.funnelback.common.io.store.RawBytesRecord;
import com.funnelback.common.io.store.Store.RecordAndMetadata;
import com.funnelback.common.io.store.bytes.FlatFileStore;

public class FilecopyFlatFileStoreCacheTest extends
        AbstractRawBytesCacheControllerTest {

    private static final String COLLECTION_ID = "cache-filecopy-flatfilestore";

    @Override
    protected String getPrimaryKey() {
        return "smb://server/share/folder/file.txt";
    }
    
    @Override
    protected void storeContent(RecordAndMetadata<RawBytesRecord> rmd) throws IOException {
        FlatFileStore store = new FlatFileStore(liveRoot);
        store.open();
        store.add(rmd.record, rmd.metadata);
        store.close();
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
