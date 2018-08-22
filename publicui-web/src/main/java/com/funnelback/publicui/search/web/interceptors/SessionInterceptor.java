package com.funnelback.publicui.search.web.interceptors;

import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.funnelback.common.profile.ProfileNotFoundException;
import com.funnelback.config.configtypes.service.ServiceConfigReadOnly;
import com.funnelback.publicui.search.web.interceptors.helpers.IntercepterHelper;
import com.funnelback.publicui.utils.web.ProfilePicker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.service.ConfigRepository;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import static com.funnelback.config.keys.Keys.FrontEndKeys;

/**
 * <p>Manage sessions and users using Cookies</p>
 * 
 * <p>Note that this is only needed for modern UI cart (search/click history)</p>
 *  
 * @since v13
 *
 */
@Log4j2
public class SessionInterceptor implements HandlerInterceptor {

    /**
     * Name of the session attribute holding the user id
     */
    public static final String SEARCH_USER_ID_ATTRIBUTE = SearchUser.class.getName()+"#id";

    private IntercepterHelper intercepterHelper = new IntercepterHelper();

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
                String profileId = new ProfilePicker().existingProfileForCollection(collection, intercepterHelper.getProfileFromRequestOrDefaultProfile(request));
                String collectionId = collection.getId();
                ServiceConfigReadOnly serviceConfig;
                try {
                    serviceConfig = configRepository.getServiceConfig(collectionId, profileId);
                    UUID uuid = getExistingOrNewUserId(request);
                    if (uuid == null) {
                        uuid = UUID.randomUUID();
                    }

                    // Set the user id in a cookie
                    if (serviceConfig.get(FrontEndKeys.ModernUi.Session.SESSION)) {
                        Cookie c = new Cookie(USER_ID_COOKIE_NAME, uuid.toString());
                        c.setMaxAge(serviceConfig.get(FrontEndKeys.ModernUi.Session.TIMEOUT));
                        response.addCookie(c);

                        // Also set it as a request attribute so that it can be accessed
                        // by controllers further down, the first time the cookie is set
                        request.setAttribute(SEARCH_USER_ID_ATTRIBUTE, uuid.toString());
                    }
                } catch (ProfileNotFoundException e) {
                    log.warn("Couldn't find profile '" + profileId + "' in " + collectionId, e);
                }
            }
        }

        return true;
    }
    
    /**
     * Get an existing user id from the cookie. If not
     * found, null is return
     * @param request HTTP request
     * @return The existing user ID, or null if not found.
     */
    public static UUID getExistingOrNewUserId(HttpServletRequest request) {
        try {
            
            if (request.getCookies() != null) {
                for (Cookie c: request.getCookies()) {
                    if (c.getName().equals(USER_ID_COOKIE_NAME) && c.getValue() != null) {
                        return UUID.fromString(c.getValue());
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
