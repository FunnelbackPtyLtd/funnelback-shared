package com.funnelback.publicui.search.web.interceptors;

import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.service.ConfigRepository;

/**
 * <p>Manage sessions and users, using the J2EE session and/or Cookies</p>
 * 
 * @since v13
 *
 */
@Log4j
public class SessionInterceptor implements HandlerInterceptor {

    /**
     * Name of the session attribute holding the user id
     */
    public static final String SEARCH_USER_ID_ATTRIBUTE = SearchUser.class.getName()+"#id";
    
    /**
     * Name of the user ID cookie to set
     */
    public static final String USER_ID_COOKIE_NAME = "user-id";
    
    @Autowired
    @Setter private ConfigRepository configRepository;
    
    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {
        
        if (request.getParameter(RequestParameters.COLLECTION) != null) {
            Collection collection = configRepository.getCollection(request.getParameter(RequestParameters.COLLECTION));
            
            if (collection != null) {
                UUID uuid = getExistingOrNewUserId(request);
                if (uuid == null) {
                    uuid = UUID.randomUUID();
                }
                
                // Store user in the J2EE session
                if (collection.getConfiguration().valueAsBoolean(Keys.ModernUI.SESSION,
                    DefaultValues.ModernUI.SESSION)) {
                    HttpSession session = request.getSession();
                    session.setAttribute(SEARCH_USER_ID_ATTRIBUTE, uuid.toString());
                    session.setMaxInactiveInterval(-1);
                }
                
                // Set a simple cookie, re-using the Session User ID if it exists
                if (collection.getConfiguration().valueAsBoolean(Keys.ModernUI.Session.SET_USERID_COOKIE,
                    DefaultValues.ModernUI.Session.SET_USERID_COOKIE)) {
                    Cookie c = new Cookie(USER_ID_COOKIE_NAME, uuid.toString());
                    c.setMaxAge(Integer.MAX_VALUE);
                    response.addCookie(c);
                    
                    // Also set it as a request attribute so that it can be accessed
                    // by controllers further down, the first time the cookie is set
                    request.setAttribute(SEARCH_USER_ID_ATTRIBUTE, uuid.toString());
                }
            }
        }

        return true;
    }
    
    /**
     * Get an existing user id either from the J2EE session or from a cookie. If not
     * found, null is return
     * @param request HTTP request
     * @return The existing user ID, or null if not found.
     */
    public static UUID getExistingOrNewUserId(HttpServletRequest request) {
        try {
            if (request.getSession(false) != null
                && request.getSession().getAttribute(SEARCH_USER_ID_ATTRIBUTE) != null) {
                return UUID.fromString((String) request.getSession().getAttribute(SEARCH_USER_ID_ATTRIBUTE));
            } else {
                if (request.getCookies() != null) {
                    for (Cookie c: request.getCookies()) {
                        if (c.getName().equals(USER_ID_COOKIE_NAME) && c.getValue() != null) {
                            return UUID.fromString(c.getValue());
                        }
                    }
                }
            }
        } catch (IllegalArgumentException iae) {
            log.warn("Invalid UUID value for user identifer", iae);
        }
        
        return null;
    }
    

    @Override
    public void postHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
        throws Exception {
    }
}
