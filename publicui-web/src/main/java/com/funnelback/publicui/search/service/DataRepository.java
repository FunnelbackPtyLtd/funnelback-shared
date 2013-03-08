package com.funnelback.publicui.search.service;

import java.io.IOException;
import java.io.InputStream;
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
     * Get a document from a file share, for filecopy collections.
     * 
     * @param collection Filecopy Collection
     * @param uri URI of the document to fetch, as in the index
     * @param withDls Whether to enforce DLS or not
     * @return An {@link InputStream} to read the document
     * @throws IOException 
     */
    public InputStream getFilecopyDocument(Collection collection, URI uri, boolean withDls) throws IOException;
    
}
