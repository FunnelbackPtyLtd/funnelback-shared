package com.funnelback.publicui.search.service.index.result;

import java.io.File;
import java.net.URI;

import com.funnelback.publicui.search.model.padre.Result;

/**
 * Fetches a result from a index
 */
public interface ResultFetcher {

    /**
     * Fetches a result from the given index
     * @param indexStem Stem of the index to get the result from
     * @param collectionName the collection name for the index stem.
     * @param resultUri URI of the result in the index
     * @return Result, or null if not found
     */
    public Result fetchResult(File indexStem, String collectionName, URI resultUri);
    
}
