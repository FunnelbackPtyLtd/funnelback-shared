package com.funnelback.publicui.test.search.web.controllers.cache.rawbytes;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

import lombok.extern.log4j.Log4j2;

import org.apache.commons.io.FileUtils;

import com.funnelback.common.cache.DummyObjectCache;
import com.funnelback.common.config.Config;
import com.funnelback.common.io.WARCStore;
import com.funnelback.common.io.WriterFile;
import com.funnelback.common.io.store.RawBytesRecord;
import com.funnelback.common.io.store.Store.RecordAndMetadata;
import com.funnelback.common.revisit.SimpleRevisitPolicy;
import com.funnelback.common.utils.DocHdrUtils;
import com.funnelback.common.views.StoreView;
import com.funnelback.common.views.View;

public class WebWarcStoreCacheTest extends
        AbstractRawBytesCacheControllerTest {

    private static final String COLLECTION_ID = "cache-web-warcstore";

    @Override
    protected String getPrimaryKey() {
        return "http://server.com/folder/file.html";
    }
    
    @Override
    protected void storeContent(RecordAndMetadata<RawBytesRecord> rmd) throws IOException {
        PrintWriter pw = new PrintWriter(System.out);
        WriterFile wf = new WriterFile(pw, null);

        // Because the WARCStore deals with views itself, we need to fake
        // using the live view to store our test data
        Config c = configRepository.getCollection(collectionId).getConfiguration();
        c.setValue("crawler.checkpoint_to",
                c.value("crawler.checkpoint_to").replace(View.offline.name(), View.live.name()));
        c.setValue("data_root", c.value("data_root").replace(View.offline.name(), View.live.name()));
        
        WARCStore ms = new WARCStore();
        ms.setUp(StoreView.live,
                liveRoot.getAbsolutePath(),
                "",
                new DummyObjectCache(),
                new DummyObjectCache(),
                new DummyObjectCache(),
                new DummyObjectCache(), new SimpleRevisitPolicy(),
                configRepository.getCollection(collectionId).getConfiguration(),
                wf,
                wf,
                wf,
                wf,
                wf,
                wf,
                0,
                0
        );
        
        ms.storeContent(
                new URL(getPrimaryKey()),
                FileUtils.readFileToByteArray(TEST_DOCUMENT),
                FileUtils.readFileToByteArray(TEST_DOCUMENT).length,
                DocHdrUtils.mapToDocHdr(METADATA),
                "text/html",
                false,
                false);
        
        ms.close();

        // Restore correct paths
        c.setValue("crawler.checkpoint_to",
                c.value("crawler.checkpoint_to").replace(View.live.name(), View.offline.name()));
        c.setValue("data_root", c.value("data_root").replace(View.live.name(), View.offline.name()));

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
