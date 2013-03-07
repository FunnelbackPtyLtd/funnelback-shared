package com.funnelback.publicui.search.service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import com.funnelback.common.io.store.Record;
import com.funnelback.common.io.store.Store;
import com.funnelback.common.io.store.Store.RecordAndMetadata;
import com.funnelback.publicui.search.model.collection.Collection;

/**
 * Interface to access collection data (stores)
 * and actual data from the original repository (Filecopy,
 * Databases, etc.)
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
	
	/**
	 * Streams a document from a file share, for filecopy collections.
	 * 
	 * @param collection Filecopy Collection
	 * @param uri URI of the document to fetch, as in the index
	 * @param withDls Whether to enforce DLS or not
	 * @param os {@link OutputStream} to stream the document to
	 */
	public void streamFilecopyDocument(Collection collection, URI uri, boolean withDls, OutputStream os) throws IOException;
	
	/**
	 * Streams a partial document from a file share, for filecopy collections
	 * @param collection Filecopy Collection
	 * @param uri URI of the document to fetch, as in the index
	 * @param withDls Wheter to enforce DLS or not
	 * @param os {@link OutputStream} to stream the document to 
	 * @param limit Number of bytes to read from the document
	 * @throws IOException
	 */
	public void streamFilecopyDocument(Collection collection, URI uri, boolean withDls, OutputStream os, int limit) throws IOException;
	
}
