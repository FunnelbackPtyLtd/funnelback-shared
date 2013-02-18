package com.funnelback.publicui.search.web.controllers.session;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.ResultsCartRepository;
import com.funnelback.publicui.search.web.interceptors.SessionInterceptor;

/**
 * Controller for the results shopping cart.
 * 
 * @since 12.4
 *
 */
@Controller
@SessionAttributes(SessionInterceptor.SEARCH_USER_ATTRIBUTE)
public class ResultsCartController extends SessionControllerBase {

	@Autowired
	private ConfigRepository configRepository;
	
	@Autowired
	private ResultsCartRepository cartRepository;

	@RequestMapping(value="/cart-list.json")
	public void cartList(
			@RequestParam("collection") String collectionId,
			@ModelAttribute(SessionInterceptor.SEARCH_USER_ATTRIBUTE) SearchUser user,
			HttpServletResponse response) throws IOException {

			Collection c = configRepository.getCollection(collectionId);
			if (c != null) {
				Map<String, Result> cart = cartRepository.getCart(user, c);
				Map<String, Object> map = new HashMap<String, Object>();
				map.put(STATUS, OK);
				map.put("cart", cart);
				map.put("cart-keys", cart.keySet());
				
				sendResponse(response, HttpServletResponse.SC_OK, map);
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
	@RequestMapping(value="/cart-add.json")
	public void cartAdd(
			Result result,
			String query,
			@ModelAttribute(SessionInterceptor.SEARCH_USER_ATTRIBUTE) SearchUser user,
			HttpServletResponse response) throws IOException {

		Collection c = configRepository.getCollection(result.getCollection());
		if (c != null) {
			cartRepository.addToCart(user, c, result, query);
			sendResponse(response, HttpServletResponse.SC_OK, OK_STATUS_MAP);
		} else {
			sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, getJsonErrorMap("Invalid collection '"+result.getCollection()+"'"));
		}
	}
	
	/**
	 * Remove a single entry from the cart, for the given collection and based
	 * on the result URL.
	 * 
	 * @param collectionId
	 * @param url
	 * @param user
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value="/cart-remove.json")
	public void cartRemove(
			@RequestParam("collection") String collectionId,
			String url,
			@ModelAttribute(SessionInterceptor.SEARCH_USER_ATTRIBUTE) SearchUser user,
			HttpServletResponse response) throws IOException {

		Collection c = configRepository.getCollection(collectionId);
		if (c != null) {
			cartRepository.removeFromCart(user, c, url);
			sendResponse(response, HttpServletResponse.SC_OK, OK_STATUS_MAP);
		} else {
			sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, getJsonErrorMap("Invalid collection '"+collectionId+"'"));
		}
	}
	
	/**
	 * Completely clear the result cart for the given collection
	 * @param collectionId
	 * @param user
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value="/cart-clear.json")
	public void cartClear(
			@RequestParam("collection") String collectionId,
			@ModelAttribute(SessionInterceptor.SEARCH_USER_ATTRIBUTE) SearchUser user,
			HttpServletResponse response) throws IOException {
		
		Collection c = configRepository.getCollection(collectionId);
		if (c != null) {
			cartRepository.clearCart(user, c);
			sendResponse(response, HttpServletResponse.SC_OK, OK_STATUS_MAP);
		} else {
			sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, getJsonErrorMap("Invalid collection '"+collectionId+"'"));
		}
	}
	
	
}
