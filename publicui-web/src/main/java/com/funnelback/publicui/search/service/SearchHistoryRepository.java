package com.funnelback.publicui.search.service;

import java.util.List;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.transaction.session.SearchHistory;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;

/**
 * Interacts with search user search history
 * 
 * @since v12.4
 */
public interface SearchHistoryRepository {
    
    /**
     * Saves a search for a given user
     * @param u
     * @param h
     */
    public void saveSearch(SearchUser u, SearchHistory h, Collection c);
    
    /**
     * @param u
     * @param maxEntries
     * @return Latest <code>maxEntries</code> entries from the user
     * search history
     */
    public List<SearchHistory> getSearchHistory(SearchUser u, Collection c, int maxEntries);
    
    /**
     * Delete all entries for the given user on the given collection
     * @param user
     * @param collection
     */
    public void clearSearchHistory(SearchUser user, Collection collection);

    /**
     * Saves a clicked result for a given user
     * @param u
     * @param r
     * @param c
     */
    public void saveClick(SearchUser u, Result r, Collection c);
    
    /**
     * @param u
     * @param c
     * @return Latest <code>maxEntries</code> results clicked
     */
    public List<Result> getClickHistory(SearchUser u, Collection c, int maxEntries);
    
    /**
     * Delete all click history entries for the given user on the given collection
     * @param user
     * @param c
     */
    public void clearClickHistory(SearchUser user, Collection c);
    
    
}
