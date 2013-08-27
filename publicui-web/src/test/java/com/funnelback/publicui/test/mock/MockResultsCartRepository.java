package com.funnelback.publicui.test.mock;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.session.CartResult;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.service.ResultsCartRepository;

public class MockResultsCartRepository implements ResultsCartRepository {

    private Map<String, Map<String, List<CartResult>>> cart = new HashMap<>();
    
    @Override
    public void addToCart(CartResult r) {
        if (cart.get(r.getUserId()) == null) {
            cart.put(r.getUserId(), new HashMap<String, List<CartResult>>());
            cart.get(r.getUserId()).put(r.getCollection(), new ArrayList<CartResult>());
        }
        
        cart.get(r.getUserId()).get(r.getCollection()).add(r);
    }

    @Override
    public void removeFromCart(SearchUser user, Collection collection, URI uri) {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    @Override
    public void clearCart(SearchUser user, Collection collection) {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    @Override
    public List<CartResult> getCart(SearchUser user, Collection collection) {
        if (cart.get(user.getId()) == null
            || cart.get(user.getId()).get(collection.getId()) == null) {
            return new ArrayList<CartResult>();
        } else {
            return cart.get(user.getId()).get(collection.getId());
        }
    }

}
