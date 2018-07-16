package com.funnelback.publicui.search.web.filters.utils;

import javax.servlet.http.HttpServletRequest;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.web.controllers.ResourcesController;

/**
 * Methods related to handling parameters that filters may need to access (like
 * the collection from the URL).
 */
public class FilterParameterHandling {

    /**
     * Look for the collection id in the query string as well
     * as in the Path part of the URI for static requests like
     * /resources/&lt;collection&gt;/&lt;profile&gt;/file.ext
     * @param request
     * @return
     */
    public String getCollectionId(HttpServletRequest request) {
        String collectionId = request.getParameter(RequestParameters.COLLECTION);
        if (collectionId == null
                && request.getPathInfo() != null
                && request.getPathInfo().startsWith(ResourcesController.MAPPING_PATH)
                && request.getPathInfo().indexOf('/', ResourcesController.MAPPING_PATH.length()) > -1) {
            collectionId = request.getPathInfo().substring(
                    ResourcesController.MAPPING_PATH.length(),
                    request.getPathInfo().indexOf('/', ResourcesController.MAPPING_PATH.length()));
        }
        
        return collectionId;
    }

    /**
     * Look for the profile id in the query string as well
     * as in the Path part of the URI for static requests like
     * /resources/&lt;collection&gt;/&lt;profile&gt;/file.ext
     * 
     * Note that for the resource URL the profile part is optional
     * and so the caller is responsible for ensuring that the profile exists.
     * @param request
     * @return
     */
    public String getProfileAndViewId(HttpServletRequest request) {
        String profileId = request.getParameter(RequestParameters.PROFILE);
        String path = request.getPathInfo();
        if (profileId == null
                && path != null
                && path.startsWith(ResourcesController.MAPPING_PATH)
                && path.indexOf('/', ResourcesController.MAPPING_PATH.length()) > -1) {
            Integer collectionStart = path.indexOf('/', ResourcesController.MAPPING_PATH.length());
            
            int endOfProfilePart = path.indexOf('/', collectionStart + 1);
            if(endOfProfilePart == -1) {
                profileId = DefaultValues.DEFAULT_PROFILE;
            } else {
                profileId = path.substring(collectionStart + 1,endOfProfilePart);
            }
        }
        
        if (profileId == null || profileId.trim().isEmpty()) {
            profileId = DefaultValues.DEFAULT_PROFILE;
        }
        
        return profileId;
    }

}
