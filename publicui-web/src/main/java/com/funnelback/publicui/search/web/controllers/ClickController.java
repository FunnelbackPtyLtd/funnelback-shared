package com.funnelback.publicui.search.web.controllers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.log4j.Log4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.log.ClickLog;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.SearchHistoryRepository;
import com.funnelback.publicui.search.service.auth.AuthTokenManager;
import com.funnelback.publicui.search.service.log.LogService;
import com.funnelback.publicui.search.service.log.LogUtils;
import com.funnelback.publicui.search.web.interceptors.SessionInterceptor;

/**
 * Click tracking controller
 */
@Log4j
@Controller
public class ClickController {

	@Autowired
	private LogService logService;

	@Autowired
	private AuthTokenManager authTokenManager;
	
	@Autowired
	private ConfigRepository configRepository;
	
	@Autowired
	private SearchHistoryRepository searchHistoryRepository;
	
	/**
	 * 
	 * @param request
	 * @param response
	 * @param collectionId
	 * @param type Faceted nav, Cluster, result click, etc.
	 * @param rank Rank of the clicked result
	 * @param profile
	 * @param redirectUrl URL to redirect to
	 * @param indexUrl URL of the clicked result from the index
	 * @param authtoken Token to check that the link was built by Funnelback
	 * @param noAttachment Special parameter to stream the content directly to the browser,
	 * used in automated testing
	 * @param result The {@link Result} object that was clicked on. It's used for click history
	 * purposes only, might be <tt>null</tt> if click history is disabled, and only some fields
	 * will be filled.
	 * @throws IOException
	 */
	@RequestMapping(value="/click", method=RequestMethod.GET)
	public void click(
			HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(RequestParameters.COLLECTION) String collectionId,
			@RequestParam(required=false,defaultValue="CLICK") ClickLog.Type type,
			Integer rank,
			@RequestParam(required=false) String profile,
			@RequestParam(value=RequestParameters.Click.URL,required=true) URI redirectUrl,
			@RequestParam(value=RequestParameters.Click.INDEX_URL,required=false) URI indexUrl,
			@RequestParam(value=RequestParameters.Click.AUTH, required=true) String authtoken,
			@RequestParam(value=RequestParameters.Click.NOATTACHMENT, required=false) String noAttachment,
			Result result) throws IOException {

		if(indexUrl == null) indexUrl = redirectUrl;
			
		Collection collection = configRepository.getCollection(collectionId);
		
		if (collection != null) {
			if(! authTokenManager.checkToken(authtoken, 
					redirectUrl.toString(), 
					collection.getConfiguration().value(Keys.SERVER_SECRET)
					)) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			}
			
			String userId = LogUtils.getUserIdentifier(request,
					DefaultValues.UserIdToLog.valueOf(collection.getConfiguration().value(Keys.USERID_TO_LOG)));
			
			URL referer = null;
			if (request.getHeader("referer") != null) {
				try {
					referer = new URL(request.getHeader("referer"));
				} catch (MalformedURLException mue) {
					log.warn("Unable to parse referer '" + request.getHeader("referer") + "'", mue);
				}
			}
			
			// TODO: Clean up how to get the user properly
			if (request.getSession() != null
					&& request.getSession().getAttribute(SessionInterceptor.SEARCH_USER_ATTRIBUTE) != null
					&& result != null) {
				searchHistoryRepository.saveClick(
						(SearchUser) request.getSession().getAttribute(SessionInterceptor.SEARCH_USER_ATTRIBUTE), 
						result, collection);
			}
			
			// FIXME: Not yet implemented
			// logService.logClick(new ClickLog(new Date(), collection, collection.getProfiles().get(profile), userId, referer, rank, indexUrl, type));
			
			response.sendRedirect(indexUrl.toString());
		} else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}
	
	// TODO remove
	@ExceptionHandler(Exception.class)
	public void handleException(Exception ex) {
		log.error(ex);
	}
	
}
