package com.funnelback.publicui.search.web.controllers.session;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.ModelAttribute;

import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.web.interceptors.SessionInterceptor;

/**
 * Common session methods that will apply to all controllers.
 * 
 * @since 13.0
 */
public class SessionController {

    /**
     * Retrieve the current search user from its ID stored in
     * the J2EE session, or directly in a cookie, or in a HTTP request
     * attribute.
     * @param request HTTP request
     * @return The corresponding search user, or null if there's no user
     * for the ID
     */
    @ModelAttribute
    public SearchUser getSearchUser(HttpServletRequest request) {
        if (request.getSession(false) != null
            && request.getSession(false).getAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE) != null) {
            return new SearchUser((String) request.getSession()
                .getAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE));
        } else {
            // Fallback to looking up cookie values
            if (request.getCookies() != null) {
                for (Cookie c: request.getCookies()) {
                    if (c.getName().equals(SessionInterceptor.USER_ID_COOKIE_NAME)) {
                        return new SearchUser(c.getValue());
                    }
                }
            }
            
            // Finally fallback to request attribute, this will be set the first
            // time the cookie is generated, since it has not be sent back by the
            // browser yet.
            if (request.getAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE) != null) {
                return new SearchUser((String) request.getAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE));
            }
        }
        
        return null;
    }

}
