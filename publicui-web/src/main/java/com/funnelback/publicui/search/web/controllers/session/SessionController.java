package com.funnelback.publicui.search.web.controllers.session;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.log4j.Log4j2;

import org.springframework.web.bind.annotation.ModelAttribute;

import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.web.interceptors.SessionInterceptor;

/**
 * Common session methods that will apply to all controllers.
 * 
 * @since 13.0
 */
@Log4j2
public abstract class SessionController {

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
        UUID uuid = SessionInterceptor.getExistingOrNewUserId(request);
        if (uuid == null) {
            // Fall back to request attribute, this will be set the first
            // time the cookie is generated, since it has not be sent back by the
            // browser yet.
            if (request.getAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE) != null) {
                String attr = (String) request.getAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE);
                try {
                    return new SearchUser(UUID.fromString(attr).toString());
                } catch (IllegalArgumentException iae) {
                    log.warn("User ID request attribute '"+attr+"' is not a UUID", iae);
                }
            }
        } else {
            return new SearchUser(uuid.toString());
        }
        
        return null;
    }

}
