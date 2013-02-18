package com.funnelback.publicui.search.web.controllers.session;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.SearchHistoryRepository;
import com.funnelback.publicui.search.web.interceptors.SessionInterceptor;

/**
 * Controller for the user search history
 * 
 * @since 12.4
 */
@Controller
@SessionAttributes(SessionInterceptor.SEARCH_USER_ATTRIBUTE)
public class SearchHistoryController extends SessionControllerBase {
	
	@Autowired
	private ConfigRepository configRepository;
	
	@Autowired
	private SearchHistoryRepository historyRepository;
	
	/**
	 * Clear the search history for the given collection.
	 * 
	 * @param collectionId
	 * @param user
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value="/search-history-clear.json")
	public void searchHistoryClear(
			@RequestParam("collection") String collectionId,
			@ModelAttribute(SessionInterceptor.SEARCH_USER_ATTRIBUTE) SearchUser user,
			HttpServletResponse response) throws IOException {

		Collection c = configRepository.getCollection(collectionId);
		if (c != null) {
			historyRepository.clearSearchHistory(user, c);
			sendResponse(response, HttpServletResponse.SC_OK, OK_STATUS_MAP);
		} else {
			sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, getJsonErrorMap("Invalid collection '"+collectionId+"'"));
		}
	}
	
	@RequestMapping(value="/click-history-clear.json")
	public void clickHistoryClear(
			@RequestParam("collection") String collectionId,
			@ModelAttribute(SessionInterceptor.SEARCH_USER_ATTRIBUTE) SearchUser user,
			HttpServletResponse response) throws IOException {

		Collection c = configRepository.getCollection(collectionId);
		if (c != null) {
			historyRepository.clearClickHistory(user, c);
			sendResponse(response, HttpServletResponse.SC_OK, OK_STATUS_MAP);
		} else {
			sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, getJsonErrorMap("Invalid collection '"+collectionId+"'"));
		}
	}
	

}
