package com.funnelback.publicui.search.service;

import java.util.List;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.usertracking.SearchHistory;
import com.funnelback.publicui.search.model.transaction.usertracking.SearchUser;

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
	public void saveHistory(SearchUser u, SearchHistory h, Collection c);
	
	/**
	 * @param u
	 * @param maxEntries
	 * @return Latest <code>maxEntries</code> entries from the user
	 * search history
	 */
	public List<SearchHistory> getHistory(SearchUser u, Collection c, int maxEntries);
	
	/**
	 * Delete all entries for the given user on the given collection
	 * @param user
	 * @param collection
	 */
	public void clearHistory(SearchUser user, Collection collection);

}
