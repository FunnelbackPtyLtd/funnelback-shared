package com.funnelback.utils;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;

/**
 * Utilities to access Redis namespace
 */
public class RedisNamespace {

	private static final String UI_SESSION_NAMESPACE = "ui:session";
	private static final String SEARCH_HISTORY_NAMESPACE = "search-history";
	private static final String CLICK_HISTORY_NAMESPACE = "click-history";
	private static final String RESULTS_CART_NAMESPACE = "results-cart";

	/**
	 * @param u
	 * @return The key to access this user search history
	 */
	public static String searchHistoryForUser(Collection c, SearchUser u) {
		if (c == null) throw new IllegalArgumentException("Collection cannot be null");
		if (u == null) throw new IllegalArgumentException("User cannot be null");
		
		return UI_SESSION_NAMESPACE+":"+SEARCH_HISTORY_NAMESPACE+":"+c.getId()+":"+u.getId();
	}
	
	public static String clickHistoryForUser(Collection c, SearchUser u) {
		if (c == null) throw new IllegalArgumentException("Collection cannot be null");
		if (u == null) throw new IllegalArgumentException("User cannot be null");
		
		return UI_SESSION_NAMESPACE+":"+CLICK_HISTORY_NAMESPACE+":"+c.getId()+":"+u.getId();
	}
	
	public static String resultsCartForUser(Collection c, SearchUser u) {
		if (c == null) throw new IllegalArgumentException("Collection cannot be null");
		if (u == null) throw new IllegalArgumentException("User cannot be null");
		
		return UI_SESSION_NAMESPACE+":"+RESULTS_CART_NAMESPACE+":"+c.getId()+":"+u.getId();
	}

}
