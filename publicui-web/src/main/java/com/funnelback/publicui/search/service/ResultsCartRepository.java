package com.funnelback.publicui.search.service;

import java.util.Map;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;

/**
 * Interacts with the user search results cart.
 * 
 * @since 12.4
 */
public interface ResultsCartRepository {

	/**
	 * Adds a single result to the cart for the given user and collection
	 * @param user
	 * @param collection
	 * @param result
	 * @param query
	 */
	public void addToCart(SearchUser user, Collection collection, Result result, String query);
	
	/**
	 * Removes a single result to the cart for the given user and collection
	 * @param user
	 * @param collection
	 * @param url URL of the result to remove
	 */
	public void removeFromCart(SearchUser user, Collection collection, String url);
	
	/**
	 * Removes all entries from the results cart for the given user and collection
	 * @param user
	 * @param collection
	 */
	public void clearCart(SearchUser user, Collection collection);
	
	/**
	 * Get the user cart for the given collection
	 * @param user
	 * @param collection
	 * @return
	 */
	public Map<String, Result> getCart(SearchUser user, Collection collection);
	
}
