package com.funnelback.publicui.search.service;

import com.funnelback.common.io.store.Record;
import com.funnelback.common.io.store.Store;
import com.funnelback.common.io.store.Store.RecordAndMetadata;
import com.funnelback.publicui.search.model.collection.Collection;

/**
 * Interface to access collection data (stores)
 */
public interface DataRepository {

	/**
	 * Gets a cached document.
	 * @param collection Collection to lookup the document from
	 * @param view View to lookup the document from (Usually the live view)
	 * @param url URL of the document
	 * @return Cached document + metadata, or null on both fields if not found
	 */
	public RecordAndMetadata<? extends Record<?>> getCachedDocument(Collection collection, Store.View view, String url);
	
}
