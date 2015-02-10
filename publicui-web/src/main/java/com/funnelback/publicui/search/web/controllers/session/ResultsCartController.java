package com.funnelback.publicui.search.web.controllers.session;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.log.CartClickLog;
import com.funnelback.publicui.search.model.log.ClickLog;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.session.CartResult;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.IndexRepository;
import com.funnelback.publicui.search.service.ResultsCartRepository;
import com.funnelback.publicui.search.service.log.LogService;
import com.funnelback.publicui.search.service.log.LogUtils;

/**
 * Controller for the results shopping cart.
 * 
 * @since 13.0
 *
 */
@Controller
@RequestMapping("/cart.json")
@Log4j
public class ResultsCartController extends SessionApiControllerBase {

    @Autowired
    private ConfigRepository configRepository;
    
    @Autowired
    private ResultsCartRepository cartRepository;

    @Autowired
    private LogService logService;

    @Autowired
    @Setter private IndexRepository indexRepository;
    
    /**
     * List the cart
     * 
     * @param collectionId Collection to list the cart for
     * @param user User to list the cart for
     * @param response HTTP response
     * @throws IOException 
     */
    @RequestMapping(method = RequestMethod.GET)
    public void cartList(
        @RequestParam("collection") String collectionId,
        @ModelAttribute SearchUser user,
        HttpServletResponse response) throws IOException {

        Collection c = configRepository.getCollection(collectionId);
        if (c != null && user != null) {
            List<CartResult> cart = cartRepository.getCart(user, c);
            sendResponse(response, HttpServletResponse.SC_OK, cart);
        } else {
            sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, KO_STATUS_MAP);
        }
    }
    
    /**
     * Add a result to the cart
     * 
     * @param collection Collection the result belongs to
     * @param url URL of the result, identical to the index one
     * @param user User for which to update the cart
     * @param response HTTP request
     * @param response HTTP response
     * @throws IOException 
     */
    @RequestMapping(method=RequestMethod.POST)
    public void cartAdd(
            @RequestParam(required = true) String collection,
            @RequestParam(required = true) URI url,
            @ModelAttribute SearchUser user,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        Collection c = configRepository.getCollection(collection);
        if (c != null && user != null) {

            Result r = indexRepository.getResult(c, url);
            if (r != null) {
                CartResult cart = CartResult.fromResult(r);
                cart.setCollection(c.getId());
                cart.setUserId(user.getId());
                cart.setAddedDate(new Date());
                
                cartRepository.addToCart(cart);
                
                logService.logCart(LogUtils.createCartLog(url, request, c, CartClickLog.Type.ADD_TO_CART, user));
            } else {
                log.warn("Result with URL '"+url+"' not found in collection '"+c.getId()+"'");
            }
            cartList(collection, user, response);
        } else {
            sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, KO_STATUS_MAP);
        }
    }
    
    /**
     * Remove a single result from the cart
     * 
     * @param collection Collection the result belongs to
     * @param url URL of the result to remove
     * @param user User for which to update the cart
     * @param response HTTP response
     * @param response HTTP response
     * @throws IOException 
     */
    @RequestMapping(method=RequestMethod.DELETE, params = RequestParameters.Cart.URL)
    public void cartRemove(
            @RequestParam(required = true) String collection,
            @RequestParam(required = true) URI url,
            @ModelAttribute SearchUser user,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        Collection c = configRepository.getCollection(collection);
        if (c != null && user != null) {

            // Removing the result from cart
            cartRepository.removeFromCart(user, c, url);
            
            logService.logCart(LogUtils.createCartLog(url, request, c, CartClickLog.Type.REMOVE_FROM_CART, user));
            
            cartList(collection, user, response);
        } else {
            sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, KO_STATUS_MAP);
        }
    }

    /**
     * Completely clear the result cart
     * @param collection Collection to clear the cart for
     * @param user User to clear the cart for
     * @param response HTTP response
     * @param response HTTP response
     * @throws IOException 
     */
    @RequestMapping(method=RequestMethod.DELETE, params = "!"+RequestParameters.Cart.URL)
    public void cartClear(
            @RequestParam String collection,
            @ModelAttribute SearchUser user,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        
        Collection c = configRepository.getCollection(collection);
        if (c != null && user != null) {
        	List<CartResult> cart = cartRepository.getCart(user, c);
        	for(CartResult result: cart) {
        		logService.logCart(LogUtils.createCartLog(result.getIndexUrl(), request, c, CartClickLog.Type.CLEAR_CART, user));
        	}
            cartRepository.clearCart(user, c);
            cartList(collection, user, response);
        } else {
            sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, KO_STATUS_MAP);
        }
    }
    
}
