package com.funnelback.publicui.search.web.interceptors;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
 * <p>Manage sessions and users.</p>
 * 
 * <p>Retrieve the user if we know about him/her, otherwise
 * creates a new user and a new session.</p>
 * 
 * @since v12.4
 *
 */
public class SessionInterceptor implements HandlerInterceptor {

    /**
     * Name of the session attribute holding the user id
     */
    public static final String SEARCH_USER_ID_ATTRIBUTE = SearchUser.class.getName()+"#id";
    
    @Autowired
    private ConfigRepository configRepository;
    
    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {
        
        if (request.getParameter(RequestParameters.COLLECTION) != null) {
            Collection collection = configRepository.getCollection(request.getParameter(RequestParameters.COLLECTION));
            if (collection != null) {
                if (collection.getConfiguration().valueAsBoolean(Keys.ModernUI.SESSION, DefaultValues.ModernUI.SESSION)) {
                    HttpSession session = request.getSession();
                    
                    if (session == null || session.getAttribute(SEARCH_USER_ID_ATTRIBUTE) == null) {
                        // New user
                        session.setMaxInactiveInterval(-1);
                        session.setAttribute(SEARCH_USER_ID_ATTRIBUTE, UUID.randomUUID().toString());
                    }
                }
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
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
        throws Exception {
    }
}
