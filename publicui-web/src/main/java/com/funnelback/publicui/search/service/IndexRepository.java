package com.funnelback.publicui.search.service;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.padre.Result;

import java.net.URI;
import java.util.Date;

/**
 * Interface to access index data
 */
public interface IndexRepository {
    
    /**
     * @return The last update date for the collection
     * @throws UnsupportedOperationException if the collection is a meta collection,
     * since they don't have a <code>index_time</code> file.
     */
    public Date getLastUpdated(String collectionId) throws UnsupportedOperationException;
    
    /**
     * Retrieve a value from the <code>.bldinfo</code> file.
     * @throws UnsupportedOperationException if the collection is a meta collection,
     * since they don't have a <code>.bldinfo</code> file.
     */
    public String getBuildInfoValue(String collectionId, String key) throws UnsupportedOperationException;

    /**
     * Retrieve a single result and all its data from the index
     * @param collection Collection to get the result from
     * @param indexUri URI of the result to get
     * @return A result, or null if not found
     */
    public Result getResult(Collection collection, URI indexUri);
}
