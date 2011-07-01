package com.funnelback.publicui.search.web.binding;

import java.util.Arrays;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import waffle.servlet.WindowsPrincipal;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.lifecycle.input.processors.PassThroughEnvironmentVariables;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.service.log.LogUtils;
import com.funnelback.publicui.utils.MapKeyFilter;
import com.funnelback.publicui.utils.MapUtils;

public class SearchQuestionBinder {
	
	/**
	 * Binds a {@link SearchQuestion} to another one by copying relevant fields.
	 * @param from
	 * @param to
	 */
	public static void bind(SearchQuestion from, SearchQuestion to) {
		to.getInputParameterMap().putAll(from.getInputParameterMap());
		to.setQuery(from.getQuery());
		to.setOriginalQuery(from.getOriginalQuery());
		to.setCollection(from.getCollection());
		to.setImpersonated(from.isImpersonated());
		to.setUserId(from.getUserId());
		to.setCnClickedCluster(from.getCnClickedCluster());
		to.getCnPreviousClusters().addAll(from.getCnPreviousClusters());
	}
	
	/**
	 * Binds properties of the given {@link SearchQuestion} to the given {@link HttpServletRequest}
	 * @param request
	 * @param question
	 */
	public static void bind(HttpServletRequest request, SearchQuestion question) {
		question.getRawInputParameters().putAll(request.getParameterMap());
		
		for (@SuppressWarnings("unchecked")
		Iterator<String> it = request.getParameterMap().keySet().iterator(); it.hasNext(); ) {
			String key = it.next();
			question.getInputParameterMap().put(key, request.getParameter(key));
		}
		
		// Add any HTTP servlet specifics 
		MapUtils.putIfNotNull(question.getInputParameterMap(), PassThroughEnvironmentVariables.Keys.REMOTE_ADDR.toString(), request.getRemoteAddr());
		MapUtils.putIfNotNull(question.getInputParameterMap(), PassThroughEnvironmentVariables.Keys.REQUEST_URI.toString(), request.getRequestURI());
		MapUtils.putIfNotNull(question.getInputParameterMap(), PassThroughEnvironmentVariables.Keys.AUTH_TYPE.toString(), request.getAuthType());
		MapUtils.putIfNotNull(question.getInputParameterMap(), PassThroughEnvironmentVariables.Keys.HTTP_HOST.toString(), request.getHeader("host"));
		MapUtils.putIfNotNull(question.getInputParameterMap(), PassThroughEnvironmentVariables.Keys.REMOTE_USER.toString(), request.getRemoteUser());

		// Referer
		MapUtils.putIfNotNull(question.getInputParameterMap(), PassThroughEnvironmentVariables.Keys.HTTP_REFERER.toString(), request.getHeader("Referer"));
				
		// Copy original query
		question.setOriginalQuery(question.getQuery());
		
		// Is request impersonated ?
		question.setImpersonated(isRequestImpersonated(request));
		
		// User identifier
		if (question.getCollection() != null && question.getCollection().getConfiguration() != null) {
			question.setUserId(LogUtils.getUserIdentifier(request,
					DefaultValues.UserIdToLog.valueOf(question.getCollection().getConfiguration().value(Keys.USERID_TO_LOG))));
		}
		
		// Last clicked cluster
		question.setCnClickedCluster(request.getParameter(RequestParameters.ContextualNavigation.CN_CLICKED));
		
		// Previously clicked clusters
		MapKeyFilter filter = new MapKeyFilter(request.getParameterMap());
		String[] paramNames = filter.filter(RequestParameters.ContextualNavigation.CN_PREV_PATTERN);
		Arrays.sort(paramNames);
		for(String paramName : paramNames) {
			// We don't really care of the indexes given in parameter names
			String value = request.getParameter(paramName);
			if (value != null && !"".equals(value) ) {
				question.getCnPreviousClusters().add(value);
			}
		}
	}

	/**
	 * <p>Detects if the request is impersonated.</p>
	 * 
	 * <p>TODO The current implementation doesn't really check if the user is impersonated
	 * but only relies on the fact that it has been authenticated using the Waffle filter.
	 * Unfortunately J2EE 5 doesn't allow us to have access to the FilterConfig to read
	 * the value of the "impersonate" parameter (Possible with J2EE 6)</p>
	 * <ul>
	 * 	<li>Either switch to J2EE 6</li>
	 * 	<li>Or update the WindowsPrincipal with a impersonation status field ?</li>
	 * </ul>
	 *   
	 * @param request
	 * @return true if the request is impersonated, false otherwise
	 */
	private static boolean isRequestImpersonated(HttpServletRequest request) {
		return request.getUserPrincipal() != null && request.getUserPrincipal() instanceof WindowsPrincipal;
	}
	
}
