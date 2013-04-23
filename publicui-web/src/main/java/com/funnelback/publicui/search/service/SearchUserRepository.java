package com.funnelback.publicui.search.service;

import com.funnelback.publicui.search.model.transaction.session.SearchUser;

/**
 * Interacts with the search users storage
 * 
 * @since 12.5
 */
public interface SearchUserRepository {

    /**
     * @param userId ID of the user
     * @return {@link SearchUser} for the given Id, or null if not found
     */
    public SearchUser getSearchUser(String userId);
    
}
