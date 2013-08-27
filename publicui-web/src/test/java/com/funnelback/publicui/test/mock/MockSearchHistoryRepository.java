package com.funnelback.publicui.test.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.session.ClickHistory;
import com.funnelback.publicui.search.model.transaction.session.SearchHistory;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.service.SearchHistoryRepository;

public class MockSearchHistoryRepository implements SearchHistoryRepository {

    private Map<String, Map<String, List<SearchHistory>>> searchHistory = new HashMap<>();
    private Map<String, Map<String, List<ClickHistory>>> clickHistory = new HashMap<>();
    
    @Override
    public void saveSearch(SearchHistory h) {
        if (searchHistory.get(h.getUserId()) == null) {
            searchHistory.put(h.getUserId(), new HashMap<String, List<SearchHistory>>());
            searchHistory.get(h.getUserId()).put(h.getCollection(), new ArrayList<SearchHistory>());
        }
        
        searchHistory.get(h.getUserId()).get(h.getCollection()).add(h);
    }

    @Override
    public List<SearchHistory> getSearchHistory(SearchUser u, Collection c, int maxEntries) {
        if (searchHistory.get(u.getId()) == null
            || searchHistory.get(u.getId()).get(c.getId()) == null) {
            return new ArrayList<SearchHistory>();
        } else {
            if (searchHistory.get(u.getId()).get(c.getId()).size() > maxEntries) {
                return searchHistory.get(u.getId()).get(c.getId()).subList(0, maxEntries-1);
            } else {
                return searchHistory.get(u.getId()).get(c.getId());
            }
        }
    }

    @Override
    public void clearSearchHistory(SearchUser u, Collection c) {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    @Override
    public void saveClick(ClickHistory h) {
        if (clickHistory.get(h.getUserId()) == null) {
            clickHistory.put(h.getUserId(), new HashMap<String, List<ClickHistory>>());
            clickHistory.get(h.getUserId()).put(h.getCollection(), new ArrayList<ClickHistory>());
        }
        
        clickHistory.get(h.getUserId()).get(h.getCollection()).add(h);
    }

    @Override
    public List<ClickHistory> getClickHistory(SearchUser u, Collection c, int maxEntries) {
        if (clickHistory.get(u.getId()) == null
            || clickHistory.get(u.getId()).get(c.getId()) == null) {
            return new ArrayList<ClickHistory>();
        } else {
            if (clickHistory.get(u.getId()).get(c.getId()).size() > maxEntries) {
                return clickHistory.get(u.getId()).get(c.getId()).subList(0, maxEntries-1);
            } else {
                return clickHistory.get(u.getId()).get(c.getId());
            }
        }
    }

    @Override
    public void clearClickHistory(SearchUser u, Collection c) {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

}
