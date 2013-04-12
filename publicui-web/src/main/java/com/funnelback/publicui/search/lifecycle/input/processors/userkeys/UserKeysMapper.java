package com.funnelback.publicui.search.lifecycle.input.processors.userkeys;

import java.util.List;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * Maps a user to access keys
 */
public interface UserKeysMapper {
    
    /**
     * @param currentCollection The collection that should be used to map users to keys
     *  This is for meta collections where the the {@link UserKeysMapper} will be called
     *  for component collection that will be different from the {@link SearchTransaction}
     *  collection.
     * @param transaction Current search
     * @return The user keys for this transaction
     */
    public List<String> getUserKeys(Collection currentCollection, SearchTransaction transaction);
    
}
