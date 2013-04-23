package com.funnelback.publicui.search.web.controllers.session;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.service.SearchUserRepository;
import com.funnelback.publicui.search.web.interceptors.SessionInterceptor;

/**
 * Common session methods that will apply to all controllers.
 * 
 * @since 12.5
 */
@ControllerAdvice
public class SessionControllerAdvice {

    @Autowired
    private SearchUserRepository searchUserRepository;

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
            return searchUserRepository.getSearchUser((String) request.getSession(false)
                .getAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE));
        }
        
        return null;
    }

}
