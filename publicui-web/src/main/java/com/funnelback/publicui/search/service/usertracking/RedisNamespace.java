package com.funnelback.publicui.search.service.usertracking;

import com.funnelback.publicui.search.model.transaction.usertracking.SearchUser;

/**
 * Utilities to access Redis namespace
 */
public class RedisNamespace {

	private static final String USER_TRACKING_NAMESPACE = "user-tracking";
	private static final String SEARCH_HISTORY_NAMESPACE = "search-history";

	/**
	 * @param u
	 * @return The key to access this user search history
	 */
	public static String searchHistoryForUser(SearchUser u) {
		return USER_TRACKING_NAMESPACE+":"+SEARCH_HISTORY_NAMESPACE+":"+u.getId();
	}

}
