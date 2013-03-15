package com.funnelback.publicui.search.web.controllers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.log4j.Log4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;

import com.funnelback.common.config.Collections;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.log.ClickLog;
import com.funnelback.publicui.search.model.log.InteractionLog;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.SearchHistoryRepository;
import com.funnelback.publicui.search.service.auth.AuthTokenManager;
import com.funnelback.publicui.search.service.log.LogService;
import com.funnelback.publicui.search.service.log.LogUtils;
import com.funnelback.publicui.search.web.binding.SearchQuestionBinder;
import com.funnelback.publicui.search.web.interceptors.SessionInterceptor;

/**
 * Click tracking controller
 */
@Log4j
@Controller
public class ClickController {

	/** Parameters not to include in the payload part of interaction logs */
	private static final String[] BORING_INTERACTION_PARAMETERS = new String[]{RequestParameters.COLLECTION,RequestParameters.Click.TYPE};

	@Autowired
	private LogService logService;
	
    /** HTTP Referer header */
    private static final String REFERER = "referer"; 
   
    @Autowired
    private AuthTokenManager authTokenManager;

    @Autowired
    private ConfigRepository configRepository;
    
    @Autowired
    private LocaleResolver localeResolver;

    @Autowired
    private SearchHistoryRepository searchHistoryRepository;
	
	
	/**
	 * Binding for interaction logging. 
	 * 
	 * @param request 
	 * @param response
	 * @param collectionId
	 * @param profile
	 * @param logType type of interaction
	 */
	@RequestMapping(value = "/log", method = RequestMethod.GET)
	public void log(
			HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(required= true, value = RequestParameters.COLLECTION) String collectionId,
			@RequestParam(required = false) String profile,
			@RequestParam(required = true, value = RequestParameters.Click.TYPE) String logType){
	
		Collection collection = configRepository.getCollection(collectionId);
		
		if (collection != null) {
			// Get the user id
			String userId = LogUtils.getUserIdentifier(request,
					DefaultValues.UserIdToLog.valueOf(collection
							.getConfiguration().value(Keys.USERID_TO_LOG)));
			
			String requestIp = getRequestIP(request);
			
			URL referer = getReferrer(request);
			
			Set<String> boringParameters =  new HashSet<String>(Arrays.asList(BORING_INTERACTION_PARAMETERS));
			
			Map<String,String[]> parameters = new HashMap<String,String[]>(request.getParameterMap());
			parameters.keySet().removeAll(boringParameters);
			
			logService.logInteraction(new InteractionLog(new Date(), collection, collection
					.getProfiles().get(profile), userId,logType,requestIp,referer,parameters));
		} else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	
	}
	
	
	/**
	 * 
     * @param request HTTP Request
     * @param response HTTP response
     * @param collectionId ID of the collection
	 * @param type Faceted nav, Cluster, result click, etc.
	 * @param rank Rank of the clicked result
	 * @param profile Current profile
	 * @param redirectUrl URL to redirect to
	 * @param authtoken Token to check that the link was built by Funnelback
	 * @param noAttachment Special parameter to stream the content directly to the browser,
	 * used in automated testing
	 * @param result The {@link Result} object that was clicked on. It's used for click history
	 * purposes only, might be <tt>null</tt> if click history is disabled, and only some fields
	 * will be filled.
	 * @throws IOException If something goes wrong
	 */
	@RequestMapping(value = "/redirect", method = RequestMethod.GET)
	public void redirect(
			HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(RequestParameters.COLLECTION) String collectionId,
			@RequestParam(required = false, defaultValue = "CLICK") ClickLog.Type type,
			Integer rank,
			@RequestParam(required = false) String profile,
			@RequestParam(value = RequestParameters.Click.URL, required = true) URI redirectUrl,
			@RequestParam(value = RequestParameters.Click.AUTH, required = true) String authtoken,
			 @RequestParam(value = RequestParameters.Click.NOATTACHMENT,
			               required = false, defaultValue = "false") boolean noAttachment,
			Result result) throws IOException {

        Collection collection = configRepository.getCollection(collectionId);
        
        if (collection != null) {
            // Does the token match the target? Forbidden if not.
            if (!authTokenManager.checkToken(authtoken, redirectUrl.toString(),
                    collection.getConfiguration().value(Keys.SERVER_SECRET))) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

			// Get the user id
			String userId = LogUtils.getUserIdentifier(request,
					DefaultValues.UserIdToLog.valueOf(collection
							.getConfiguration().value(Keys.USERID_TO_LOG)));
			
			String requestIp = getRequestIP(request);
			URL referer = getReferrer(request);
			
			// TODO: Clean up how to get the user properly
			if (request.getSession() != null
					&& request.getSession().getAttribute(SessionInterceptor.SEARCH_USER_ATTRIBUTE) != null
					&& result != null) {
				searchHistoryRepository.saveClick(
						(SearchUser) request.getSession().getAttribute(SessionInterceptor.SEARCH_USER_ATTRIBUTE), 
						result, collection);
			}
			logService.logClick(new ClickLog(new Date(), collection, collection
					.getProfiles().get(profile), userId, referer, rank,
					redirectUrl, type, requestIp));
			
            response.sendRedirect(
                redirectUrl.toString()
                + ( noAttachment
                    ? "&"+RequestParameters.Click.NOATTACHMENT+"="+Boolean.toString(noAttachment)
                    : "")
            );
		} else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}

	/**
	 * Helper method to get the request IP address from a request.
	 * 
	 * @param request
	 * @return the string representation of the requesting IP address
	 */
	private String getRequestIP(HttpServletRequest request) {
		/* TODO: We only use the SearchQuestion to get the request IP
		 * Perhaps we should have a helper to do the same thing from the request?
		 */
		SearchQuestion question = new SearchQuestion();
		SearchQuestionBinder.bind(request, question, localeResolver);
		String requestIp = SearchQuestionBinder.getRequestIp(question);
		return requestIp;
	}

	/**
	 * Helper method to get the HTTP referrer out of a request
	 * 
	 * @param request 
	 * @return the URL for the HTTP referrer.
	 */
	private URL getReferrer(HttpServletRequest request) {
		URL referer = null;
		if (request.getHeader("referer") != null) {
			try {
				referer = new URL(request.getHeader("referer"));
			} catch (MalformedURLException mue) {
				log.warn(
						"Unable to parse referer '"
								+ request.getHeader("referer") + "'", mue);
			}
		}
		return referer;
	}

}
