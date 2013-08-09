package com.funnelback.publicui.search.web.controllers.session;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.SneakyThrows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.session.CartResult;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.IndexRepository;
import com.funnelback.publicui.search.service.ResultsCartRepository;

/**
 * Controller for the results shopping cart.
 * 
 * @since 12.4
 *
 */
@Controller
@RequestMapping("/cart.json")
public class ResultsCartController extends SessionApiControllerBase {

    @Autowired
    private ConfigRepository configRepository;
    
    @Autowired
    private ResultsCartRepository cartRepository;

    @Autowired
    private IndexRepository indexRepository;
    
    @RequestMapping(method = RequestMethod.GET)
    public void cartList(
        @RequestParam("collection") String collectionId,
        @ModelAttribute SearchUser user,
        HttpServletResponse response) throws IOException {

        Collection c = configRepository.getCollection(collectionId);
        if (c != null) {
            List<CartResult> cart = cartRepository.getCart(user, c);
            sendResponse(response, HttpServletResponse.SC_OK, cart);
        } else {
            sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, getJsonErrorMap("Invalid collection '"+collectionId+"'"));
        }
    }
    
    /**
     * Adds a single result to the cart. Related collection is taken
     * from the result itself.
     * 
     * @param result
     * @param query
     * @param user
     * @param response
     * @throws IOException
     */
    @RequestMapping(method=RequestMethod.POST)
    public void cartAdd(
            @RequestParam(required = true) String collection,
            @RequestParam(required = true) URI url,
            @ModelAttribute SearchUser user,
            HttpServletResponse response) throws IOException {

        Collection c = configRepository.getCollection(collection);
        if (c != null) {

            Result r = indexRepository.getResult(c, url);

            CartResult result = CartResult.fromResult(r);
            result.setUserId(user.getId());
            result.setAddedDate(new Date());
            cartRepository.addToCart(result);
            cartList(collection, user, response);
        } else {
            sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, getJsonErrorMap("Invalid collection '"+collection+"'"));
        }
    }
    
    /**
     * Remove a single entry from the cart, for the given collection and based
     * on the result URL.
     * 
     * @param collection
     * @param url
     * @param user
     * @param response
     * @throws IOException
     */
    @RequestMapping(method=RequestMethod.DELETE, params = RequestParameters.Cart.URL)
    public void cartRemove(
            @RequestParam(required = true) String collection,
            @RequestParam(required = true) URI url,
            @ModelAttribute SearchUser user,
            HttpServletResponse response) throws IOException {

        Collection c = configRepository.getCollection(collection);
        if (c != null) {
            cartRepository.removeFromCart(user, c, url);
            cartList(collection, user, response);
        } else {
            sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, getJsonErrorMap("Invalid collection '"+collection+"'"));
        }
    }
    
    /**
     * Completely clear the result cart for the given collection
     * @param collection
     * @param user
     * @param response
     * @throws IOException
     */
    @RequestMapping(method=RequestMethod.DELETE, params = "!"+RequestParameters.Cart.URL)
    public void cartClear(
            @RequestParam String collection,
            @ModelAttribute SearchUser user,
            HttpServletResponse response) throws IOException {
        
        Collection c = configRepository.getCollection(collection);
        if (c != null) {
            cartRepository.clearCart(user, c);
            cartList(collection, user, response);
        } else {
            sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, getJsonErrorMap("Invalid collection '"+collection+"'"));
        }
    }

    @SneakyThrows(MalformedURLException.class)
    private String getSearchUrl(HttpServletRequest request, Collection c) {
        URL u = new URL(request.getRequestURL().toString());
        
        return u.getProtocol() + "://"
                + u.getHost()
                + ((u.getPort() > 0) ? ":"+u.getPort() : "")
                + u.getPath().substring(0, u.getPath().lastIndexOf('/')+1)
                + c.getConfiguration().value(Keys.ModernUI.SEARCH_LINK, DefaultValues.ModernUI.SEARCH_LINK)
                + "?" + RequestParameters.COLLECTION + "=" + c.getId();        
    }
    
    
}
