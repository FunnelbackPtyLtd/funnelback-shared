package com.funnelback.publicui.search.web.interceptors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.service.ConfigRepository;

/**
 * Checks if early or late binding DLS is enabled, 
 * and then denys access if either is enabled.
 */
public class DenyIfDlsIsOnInterceptor implements HandlerInterceptor {

	@Override
	public void afterCompletion(HttpServletRequest arg0,
			HttpServletResponse arg1, Object arg2, Exception arg3)
			throws Exception { }

	@Override
	public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1,
			Object arg2, ModelAndView arg3) throws Exception {	}
	
	@Autowired
	private ConfigRepository configRepository;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
			Object handler) throws Exception {
		if (request.getParameter(RequestParameters.COLLECTION) != null) {
			Collection c = configRepository.getCollection(request.getParameter(RequestParameters.COLLECTION));
			if(c == null) return true;
			
			String earlyBindingValue = c.getConfiguration().value(Keys.SecurityEarlyBinding.USER_TO_KEY_MAPPER );
			if(earlyBindingValue != null && !("".equals(earlyBindingValue))) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return false;
			} 
				
			String dlsValue = c.getConfiguration().value(Keys.DocumentLevelSecurity.DOCUMENT_LEVEL_SECURITY_MODE);
			if(dlsValue != null && !"disabled".equals(dlsValue)) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return false;
			}
		} 
		
		return true;
	}

}
