package com.funnelback.publicui.test.mock;

import java.util.List;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.session.ClickHistory;
import com.funnelback.publicui.search.model.transaction.session.SearchHistory;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.service.SearchHistoryRepository;

public class MockSearchHistoryRepository implements SearchHistoryRepository {

    @Override
    public void saveSearch(SearchHistory h) {
    }

    @Override
    public List<SearchHistory> getSearchHistory(SearchUser u, Collection c, int maxEntries) {
        return null;
    }

    @Override
    public void clearSearchHistory(SearchUser u, Collection c) {
    }

    @Override
    public void saveClick(ClickHistory h) {
    }

    @Override
    public List<ClickHistory> getClickHistory(SearchUser u, Collection c, int maxEntries) {
        return null;
    }

    @Override
    public void clearClickHistory(SearchUser u, Collection c) {
    }

}
