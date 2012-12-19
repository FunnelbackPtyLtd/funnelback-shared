package com.funnelback.publicui.test.search.web.controllers.cache.xml;

import java.io.IOException;

import com.funnelback.common.io.store.Store.RecordAndMetadata;
import com.funnelback.common.io.store.XmlRecord;
import com.funnelback.common.io.store.xml.FlatFileStore;

public class ConnectorFlatFileStoreCacheTest extends
		AbstractXmlCacheControllerTest {

	private static final String COLLECTION_ID = "cache-connector-flatfilestore";
	
	@Override
	protected void storeContent(RecordAndMetadata<XmlRecord> rmd)
			throws IOException {
		FlatFileStore store = new FlatFileStore(liveRoot);
		store.open();
		store.add(rmd.record);
		store.close();

	}

	@Override
	protected String getPrimaryKey() {
		return "https://exchange2007.harness.local/owa?ae=Folder&id=LgAAAAD9UFRvllr3TLdKKZ9cbQzmAQDIbcuzRQWYQLNSRcGj6iIrAAAAADwUAAAB";
	}

	@Override
	protected String getCollectionId() {
		return COLLECTION_ID;
	}

	@Override
	protected String getCacheUrl(String primaryKey) {
		return getPrimaryKey();
	}

}
