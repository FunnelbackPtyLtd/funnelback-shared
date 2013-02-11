package com.funnelback.publicui.search.web.controllers;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import lombok.extern.log4j.Log4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.transaction.usertracking.SearchUser;
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
@Log4j
public class ResultsCartController {

	private static final String OK = "OK";
	private static final String KO = "KO";
	
	@Autowired
	private ConfigRepository configRepository;
	
	@Autowired
	private ResultsCartRepository cartRepository;

	@RequestMapping(value="/cart-add.html")
	public void cartAdd(
			Result result,
			String query,
			@ModelAttribute(SessionInterceptor.SEARCH_USER_ATTRIBUTE) SearchUser user,
			HttpServletResponse response) throws IOException {
		
		//FIXME check collection
		cartRepository.addToCart(user, configRepository.getCollection(result.getCollection()), result, query);

		sendResponse(response, HttpServletResponse.SC_OK, OK);
	}
	
	@RequestMapping(value="/cart-remove.html")
	public void cartRemove(
			@RequestParam("collection") String collectionId,
			String url,
			@ModelAttribute(SessionInterceptor.SEARCH_USER_ATTRIBUTE) SearchUser user,
			HttpServletResponse response) throws IOException {
		
		// FIXME Check collection
		cartRepository.removeFromCart(user, configRepository.getCollection(collectionId), url);

		sendResponse(response, HttpServletResponse.SC_OK, OK);
	}
	
	@RequestMapping(value="/cart-clear.html")
	public void cartClear(
			@RequestParam("collection") String collectionId,
			String url,
			@ModelAttribute(SessionInterceptor.SEARCH_USER_ATTRIBUTE) SearchUser user,
			HttpServletResponse response) throws IOException {
		
		// FIXME Check collection
		cartRepository.clearCart(user, configRepository.getCollection(collectionId));
		sendResponse(response, HttpServletResponse.SC_OK, OK);
	}
	
	private void sendResponse(HttpServletResponse response, int status, String msg) throws IOException {
		response.setStatus(status);
		response.setContentType("text/plain");
		if (msg != null) {
			response.getWriter().write(msg);
		}
	}
	
	@ExceptionHandler(DataAccessException.class)
	public void exceptionHandler(Exception e, HttpServletResponse response) throws IOException {
		log.error("Error while processing results cart", e);
		sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, KO + " " + e.toString());
	}
	
}
