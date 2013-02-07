package com.funnelback.publicui.search.web.interceptors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.service.ConfigRepository;

/**
 * Checks if early or late binding DLS is enabled, 
 * and then denies access if either is enabled.
 */
public class DenyIfDlsIsOnInterceptor implements HandlerInterceptor {

	@Autowired
	private ConfigRepository configRepository;

	@Override
	public void afterCompletion(HttpServletRequest request,
			HttpServletResponse response, Object o, Exception e)
			throws Exception { }

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response,
			Object o, ModelAndView mav) throws Exception {	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
			Object handler) throws Exception {
		if (request.getParameter(RequestParameters.COLLECTION) != null) {
			Collection collection = configRepository.getCollection(request.getParameter(RequestParameters.COLLECTION));
			if(collection == null) {
				return true;
			}
			
			Config c = collection.getConfiguration();
			
			if (c.hasValue(Keys.SecurityEarlyBinding.USER_TO_KEY_MAPPER)
					|| ( c.hasValue(Keys.DocumentLevelSecurity.DOCUMENT_LEVEL_SECURITY_MODE)
							&& ! Config.isFalse(c.value(Keys.DocumentLevelSecurity.DOCUMENT_LEVEL_SECURITY_MODE)))) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return false;
			}
		} 
		
		return true;
	}

}
