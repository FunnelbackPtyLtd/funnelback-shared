package com.funnelback.publicui.search.web.controllers.session;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import com.funnelback.common.config.Files;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.session.CartResult;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;

/**
 * Class to be implemented to process a results cart.
 * 
 * @since 13.0
 */
public class CartProcessController {

    /**
     * Processes the cart
     * @param collection Collection where processing takes place
     * @param user Current user from session
     * @param cart Results cart, for the given collection and user
     * @return A {@link ModelAndView} containing the data and the view to use
     */
    public ModelAndView process(Collection collection, SearchUser user, List<CartResult> cart) {
        return new ModelAndView(Files.CART_PROCESS_PREFIX);
    }
    
    /**
     * Processes the cart, giving access to the HTTP response for fine grained control of the output
     * @param collection Collection where processing takes place
     * @param user Current user from session
     * @param cart Results cart, for the given collection and user
     * @param response HTTP response
     * @return A {@link ModelAndView} containing the data and the view to use
     */
    public ModelAndView process(Collection collection, SearchUser user, List<CartResult> cart, HttpServletResponse response) {
        return process(collection, user, cart);
    }
    
}
