package com.funnelback.publicui.test.mock;

import java.util.HashMap;
import java.util.Map;

import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.service.SearchUserRepository;

public class MockSearchUserRepository implements SearchUserRepository {
    
    private final Map<String, SearchUser> users = new HashMap<>();

    @Override
    public SearchUser getSearchUser(String userId) {
        return users.get(userId);
    }

    @Override
    public void createSearchUser(SearchUser user) { }

}
