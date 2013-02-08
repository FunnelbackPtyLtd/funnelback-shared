package com.funnelback.publicui.search.service;

import java.util.List;

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
	public void save(SearchUser u, SearchHistory h);
	
	/**
	 * @param u
	 * @param maxEntries
	 * @return Latest <code>maxEntries</code> entries from the user
	 * search history
	 */
	public List<SearchHistory> get(SearchUser u, int maxEntries);

}
