package com.funnelback.publicui.search.service.user;

import org.springframework.stereotype.Repository;

import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.service.SearchUserRepository;

/**
 * Default repository for search users.
 */
@Repository
public class DefaultSearchUserRepository implements SearchUserRepository {

    @Override
    public SearchUser getSearchUser(String userId) {
        return new SearchUser(userId);
    }

}
