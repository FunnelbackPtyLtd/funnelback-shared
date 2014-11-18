package com.funnelback.publicui.test.search.web.controllers.cache.rawbytes;

import com.funnelback.common.io.store.RawBytesRecord;
import com.funnelback.common.io.store.Store.RecordAndMetadata;
import com.funnelback.common.io.store.bytes.WarcFileStore;

import java.io.IOException;

public class FilecopyWarcFileStoreCacheTest extends
        AbstractRawBytesCacheControllerTest {

    private static final String COLLECTION_ID = "cache-filecopy-warcfilestore";

    @Override
    protected String getPrimaryKey() {
        return "smb://server/share/folder/file.txt";
    }
    
    @Override
    protected void storeContent(RecordAndMetadata<RawBytesRecord> rmd) throws IOException {
        WarcFileStore store = new WarcFileStore(
                liveRoot);
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
