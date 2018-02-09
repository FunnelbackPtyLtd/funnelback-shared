package com.funnelback.publicui.search.web.interceptors.helpers;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;


/**
 * A helper class for Funnelback specific intercepters
 *
 */
public class IntercepterHelper {

    public boolean requestHasValidCollectionId(HttpServletRequest request) {
        return request.getParameter(RequestParameters.COLLECTION) != null
            && request.getParameter(RequestParameters.COLLECTION).matches(Collection.COLLECTION_ID_PATTERN);
    }
    
    public String getCollectionFromRequest(HttpServletRequest request) {
        return request.getParameter(RequestParameters.COLLECTION);
    }
    
    public String getProfileFromRequestOrDefaultProfile(HttpServletRequest request) {
        String profileId = request.getParameter(RequestParameters.PROFILE);
        
        if (profileId == null || profileId.trim().isEmpty()) {
            profileId = DefaultValues.DEFAULT_PROFILE;
        }
        
        return profileId;
    }
}
