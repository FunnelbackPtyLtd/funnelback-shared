package com.funnelback.publicui.search.lifecycle.input.processors.userkeys;

import java.util.List;

import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * Maps a user to access keys
 */
public interface UserKeysMapper {
    
    /**
     * @param transaction Current search
     * @return The user keys for this transaction
     */
    public List<String> getUserKeys(SearchTransaction transaction);
    
}
