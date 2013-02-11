package com.funnelback.utils;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.usertracking.SearchUser;

/**
 * Utilities to access Redis namespace
 */
public class RedisNamespace {

	private static final String USER_TRACKING_NAMESPACE = "user-tracking";
	private static final String SEARCH_HISTORY_NAMESPACE = "search-history";
	private static final String RESULTS_CART_NAMESPACE = "results-cart";

	/**
	 * @param u
	 * @return The key to access this user search history
	 */
	public static String searchHistoryForUser(Collection c, SearchUser u) {
		if (c == null) throw new IllegalArgumentException("Collection cannot be null");
		if (u == null) throw new IllegalArgumentException("User cannot be null");
		
		return USER_TRACKING_NAMESPACE+":"+SEARCH_HISTORY_NAMESPACE+":"+c.getId()+":"+u.getId();
	}
	
	public static String resultsCartForUser(Collection c, SearchUser u) {
		if (c == null) throw new IllegalArgumentException("Collection cannot be null");
		if (u == null) throw new IllegalArgumentException("User cannot be null");
		
		return USER_TRACKING_NAMESPACE+":"+RESULTS_CART_NAMESPACE+":"+c.getId()+":"+u.getId();
	}

}
