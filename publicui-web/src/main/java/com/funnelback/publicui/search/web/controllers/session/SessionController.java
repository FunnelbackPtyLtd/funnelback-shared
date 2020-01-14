package com.funnelback.publicui.search.web.controllers.session;

import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.profile.ProfileNotFoundException;
import com.funnelback.config.configtypes.service.ServiceConfigReadOnly;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.service.ConfigRepository;
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
     * the cookie, or in a HTTP request attribute.
     * @param request HTTP request
     * @return The corresponding search user, or null if there's no user
     * for the ID
     */
    @ModelAttribute
    public final SearchUser getSearchUser(HttpServletRequest request) {
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

    /**
     * Gets the Service config for the given profile, if no profile is given or the profile does not exist the default
     * is returned.
     *
     * @param configRepository
     * @param collection
     * @param profile
     * @return
     * @throws IllegalStateException when both the given profile and default profile do not exist.
     */
    public final ServiceConfigReadOnly getServiceConfigOrDefault(ConfigRepository configRepository, Collection collection, Optional<String> profile)
        throws IllegalStateException {
        if(profile.isPresent()) {
            try {
                return configRepository.getServiceConfig(collection.getId(), profile.get());
            } catch (ProfileNotFoundException e) {
                log.warn("Given prrofile {} does not exist reverting to default profile", profile.get());
            }
        }
        try {
            return configRepository.getServiceConfig(collection.getId(), DefaultValues.DEFAULT_PROFILE);
        } catch (ProfileNotFoundException e) {
            throw new IllegalStateException("The default profile is missing, it must be created if the collection still exists", e);
        }
    }
}
