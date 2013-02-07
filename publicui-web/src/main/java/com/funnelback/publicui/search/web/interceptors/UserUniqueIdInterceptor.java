package com.funnelback.publicui.search.web.interceptors;

import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.log4j.Log4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.service.ConfigRepository;

/**
 * Assigns a unique ID to the visitor if it doesn't already
 * have one (cookie based).
 * 
 * @since v12.4
 *
 */
@Log4j
public class UserUniqueIdInterceptor implements HandlerInterceptor {

	public static final String USER_ID_REQUEST_ATTRIBUTE = "com.funnelback.publicui.search.USER_ID";
	public static final String COOKIE_NAME = "funnelback-user-id";
	private static final int COOKIE_MAX_AGE = 60*60*24*365*10;
	
	@Autowired
	private ConfigRepository configRepository;
	
	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {

		if (request.getParameter(RequestParameters.COLLECTION) != null) {
			Collection collection = configRepository.getCollection(request.getParameter(RequestParameters.COLLECTION));
			if (collection.getConfiguration().valueAsBoolean(Keys.ModernUI.USER_ID_COOKIE, DefaultValues.ModernUI.USER_ID_COOKIE)) {

				// Do not initialize with randomUUID(). Possibly expensive ?
				UUID uuid = null;
				
				Cookie c = getIdCookie(request);
				
				if (c == null) {
					uuid = UUID.randomUUID();
					addIdCookie(request, response, uuid);
				} else {
					try {
						uuid = UUID.fromString(c.getValue());
					} catch (IllegalArgumentException iae) {
						log.warn("Discarding invalid user id '"+c.getValue()+"' stored in cookie '"+ COOKIE_NAME+"'");
						uuid = UUID.randomUUID();
						addIdCookie(request, response, uuid);
					}
				}
				
				request.setAttribute(USER_ID_REQUEST_ATTRIBUTE, uuid.toString());
			}
		}

		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
	}

	@Override
	public void afterCompletion(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
	}

	private Cookie getIdCookie(HttpServletRequest request) {
		if (request.getCookies() != null) {
			for (Cookie c: request.getCookies()) {
				if (COOKIE_NAME.equals(c.getName())) {
					return c;
				}
			}
		}
		
		return null;
	}
	
	private void addIdCookie(HttpServletRequest request, HttpServletResponse response, UUID uuid) {
		Cookie c = new Cookie(COOKIE_NAME, uuid.toString());

		c.setMaxAge(COOKIE_MAX_AGE);
		c.setPath(request.getRequestURI().substring(0, request.getRequestURI().lastIndexOf('/')));

		// DO NOT USE on localhost: Some browser fail to set the cookie
		// c.setDomain(requestUrl.getHost());
		
		response.addCookie(c);
	}
}
