package com.funnelback.publicui.search.service;

import java.net.URI;
import java.util.List;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.session.CartResult;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;

/**
 * Interacts with the user search results cart.
 * 
 * @since 12.4
 */
public interface ResultsCartRepository {

    /**
     * Adds a single result to the cart
     * @param result {@link CartResult} to add
     */
    public void addToCart(CartResult result);
    
    /**
     * Removes a single result to the cart for the given user and collection
     * @param user User to remove the cart entry from
     * @param collection Collection on which remove the cart entry
     * @param uri URI of the result to remove
     */
    public void removeFromCart(SearchUser user, Collection collection, URI uri);
    
    /**
     * Removes all entries from the results cart for the given user and collection
     * @param user User to clear the cart from
     * @param collection Collection to clear the cart from
     */
    public void clearCart(SearchUser user, Collection collection);
    
    /**
     * @param user User to get the cart from
     * @param collection Collection to get the cart from
     * @return The user results cart for the given collection 
     */
    public List<CartResult> getCart(SearchUser user, Collection collection);
    
    /**
     * Purge old carts
     * @param daysToKeep How many days of carts to keep
     * purged, in days
     * @return Number of session purged
     */
    public int purgeCartResults(int daysToKeep);
    
}
