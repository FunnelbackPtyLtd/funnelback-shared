package com.funnelback.publicui.search.web.interceptors;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.common.config.CollectionId;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.profile.ProfileId;
import com.funnelback.common.profile.ProfileNotFoundException;
import com.funnelback.config.configtypes.service.ServiceConfigReadOnly;
import com.funnelback.config.keys.Keys.FrontEndKeys;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.ExecutionContext;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.web.exception.InvalidCollectionException;
import com.funnelback.publicui.search.web.interceptors.helpers.IntercepterHelper;
import com.funnelback.publicui.utils.web.ExecutionContextHolder;
import com.funnelback.publicui.utils.web.ProfilePicker;
import com.funnelback.springmvc.api.config.security.user.model.FunnelbackUser;
import com.funnelback.springmvc.web.security.CurrentFunnelbackUserHelper;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class RestrictAccessToPreviewProfile implements HandlerInterceptor {

    @Autowired
    @Setter(AccessLevel.PROTECTED)
    private ConfigRepository configRepository;
    
    @Autowired
    private File searchHome;
    
    @Autowired @Setter
    private ExecutionContextHolder executionContextHolder;
    
    @Autowired @Setter
    private I18n i18n;
    
    @Setter
    private CurrentFunnelbackUserHelper currentFunnelbackUserHelper = new CurrentFunnelbackUserHelper();
    
    @Setter 
    private IntercepterHelper intercepterHelper = new IntercepterHelper();
    
    @Setter 
    private ProfilePicker profilePicker = new ProfilePicker();
    
    @Override
    public void afterCompletion(HttpServletRequest request,
        HttpServletResponse response, Object handler, Exception exception)
        throws Exception {
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler, ModelAndView modelAndView) throws Exception {
    }

    
    
    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {
        
        String collectionId = intercepterHelper.getCollectionFromRequest(request);
        
        if(collectionId == null) {
            log.trace("allowing access when no collection is set.");
            return true;
        }
        
        Collection collection = configRepository.getCollection(collectionId);
        
        if (collection == null) {
            // You asked for a collection which doesn't exist, so no access restriction applies.
            // (otherwise no one could ever get to the collection listing page)
            log.trace("allowing access to nonexistent collection" + collectionId);
            return true;
        }
        
        String profileName = profilePicker.existingProfileForCollection(collection, 
                                intercepterHelper.getProfileFromRequestOrDefaultProfile(request));
        
        if(!profileName.endsWith(DefaultValues.PREVIEW_SUFFIX)) {
            log.trace("allowing access to non preview profile" + collectionId);
            return true;
        }
        
        ServiceConfigReadOnly serviceConfig;
        try {
            serviceConfig = configRepository.getServiceConfig(collectionId, profileName);
        } catch (ProfileNotFoundException e) {
            // profile picker always returns a profile that it thinks exists or one that is supposed to exist e.g. defailt_profile.
            throw new InvalidCollectionException(collectionId + " appears to exist but is invalid as it is missing the '" 
                                                    + profileName + "' profile which is expected to exist.");
        }
        
        if(!serviceConfig.get(FrontEndKeys.RESTRICT_PREVIEW_TO_AUTHENTICATED_USERS)) {
            log.trace("allowing access to preview profile as the profile is not configured to restrict access.");
            return true;
        }
        
        
        
        if(executionContextHolder.getExecutionContext() == ExecutionContext.Admin) {
            FunnelbackUser user = currentFunnelbackUserHelper.getCurrentFunnelbackUser();
            boolean hasAccessToColl = user.getUserInfoDetails().getCollectionRestriction().isPermitted(new CollectionId(collectionId));
            if(hasAccessToColl) {
                
                String profileId = profileName.substring(0, profileName.length() - DefaultValues.PREVIEW_SUFFIX.length());
                if(!profileName.equals(profileId + DefaultValues.PREVIEW_SUFFIX)) {
                    throw new RuntimeException("Could not correctly strip _preview suffix from " + profileId);
                }
                
                boolean hasAccessToProfile = user.getUserInfoDetails().getProfileRestriction().isPermitted(new ProfileId(profileId));
                if(hasAccessToProfile) {
                    log.trace("allowing access to collection: {} and preview profile: {} as the user is authenticated and has access to this collection.",
                        collection, profileName);
                    return true;
                }
                log.trace("preventing access to preview profile {} as the user does not have access to this profile.", profileName);
            } else {
                log.trace("preventing access to {} preview profile as the user does not have access to this collection.", collection);
            }
            
        }
        
        denyAccess(response);
        
        return false;
    }
    
    void denyAccess(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("text/plain");
        response.getWriter().write(i18n.tr("access.profile.preview.denied"));
    }
}
