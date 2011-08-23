package com.funnelback.publicui.search.web.interceptors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.common.config.Keys;
import com.funnelback.contentoptimiser.ContentOptimiserUserRestrictions;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.service.ConfigRepository;

/**
 * Checks if early or late binding DLS is enabled, and then denys access if
 * either is enabled.
 */
public class ContentOptimiserRestrictionInterceptor implements
		HandlerInterceptor {

	@Override
	public void afterCompletion(HttpServletRequest arg0,
			HttpServletResponse arg1, Object arg2, Exception arg3)
			throws Exception {
	}

	@Override
	public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1,
			Object arg2, ModelAndView arg3) throws Exception {
	}

	@Autowired
	private ConfigRepository configRepository;

	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		if (request.getParameter(RequestParameters.COLLECTION) != null) {
			Collection c = configRepository.getCollection(request
					.getParameter(RequestParameters.COLLECTION));
			if (c == null)
				return true;
			String adminPort = c.getConfiguration().value(Keys.Urls.ADMIN_PORT);
			ContentOptimiserUserRestrictions restrictions = new ContentOptimiserUserRestrictions(c.getConfiguration().value(
					Keys.CONTENT_OPTIMISER_NON_ADMIN_ACCESS),adminPort != null
					&& (new Integer(request.getLocalPort()).toString())
					.equals(adminPort));
			request.setAttribute(ContentOptimiserUserRestrictions.class.getName(),restrictions);
			
			
			if (restrictions.isOnAdminPort()) {
				// always allow access on the admin port
				return true;
			}
			
			// if we're here, we're not on the admin port
			if (restrictions.isAllowNonAdminFullAccess()) {
				return true;
			} else if (restrictions.isAllowNonAdminTextAccess()) {
				if (request
						.getParameter(RequestParameters.CONTENT_OPTIMISER_ADVANCED) != null) {
					// this is the advanced view, so say no
					response.setStatus(HttpServletResponse.SC_FORBIDDEN);
					return false;
				} else {
					return true;
				}
			} else  {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return false;
			}
		}
		// we've got no collection to ask if we should allow the content
		// optimiser
		return true;
	}

}
