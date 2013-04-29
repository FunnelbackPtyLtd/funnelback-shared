package com.funnelback.publicui.search.service;

import java.util.List;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.transaction.session.ClickHistory;
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
     * @param h Search to save
     */
    public void saveSearch(SearchHistory h);
    
    /**
     * @param u User to lookup the history for
     * @param c collection to lookup the history for
     * @param maxEntries Maximum number of search history entries to return
     * @return Latest <code>maxEntries</code> entries from the user
     * search history
     */
    public List<SearchHistory> getSearchHistory(SearchUser u, Collection c, int maxEntries);
    
    /**
     * Delete all entries for the given user on the given collection
     * @param u User to delete the history for
     * @param c Collection to delete the history for
     */
    public void clearSearchHistory(SearchUser u, Collection c);

    /**
     * Saves a clicked result for a given user
     * @param h Click to save
     */
    public void saveClick(ClickHistory h);
    
    /**
     * @param u user to lookup the history for
     * @param c Collection to lookup the history for
     * @param maxEntries Maximum number of click history entries to return
     * @return Latest <code>maxEntries</code> results clicked
     */
    public List<ClickHistory> getClickHistory(SearchUser u, Collection c, int maxEntries);
    
    /**
     * Delete all click history entries for the given user on the given collection
     * @param u User to delete the history for
     * @param c Collection to delete the history for
     */
    public void clearClickHistory(SearchUser u, Collection c);
    
    
}
