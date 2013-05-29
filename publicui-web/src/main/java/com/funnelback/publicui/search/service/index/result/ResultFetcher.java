package com.funnelback.publicui.search.service.index.result;

import com.funnelback.publicui.search.model.padre.Result;

import java.io.File;
import java.net.URI;

/**
 * Fetches a result from a index
 */
public interface ResultFetcher {

    /**
     * Fetches a result from the given index
     * @param indexStem Stem of the index to get the result from
     * @param resultUri URI of the result in the index
     * @return Result, or null if not found
     */
    public Result fetchResult(File indexStem, URI resultUri);
    
}
