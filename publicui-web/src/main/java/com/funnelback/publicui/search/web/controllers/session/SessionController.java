package com.funnelback.publicui.search.web.controllers.session;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.ModelAttribute;

import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.web.interceptors.SessionInterceptor;

/**
 * Common session methods that will apply to all controllers.
 * 
 * @since 12.5
 */
public class SessionController {

    /**
     * Retrieve the current search user from its ID stored in
     * the J2EE session.
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
        }
        
        return null;
    }

}
