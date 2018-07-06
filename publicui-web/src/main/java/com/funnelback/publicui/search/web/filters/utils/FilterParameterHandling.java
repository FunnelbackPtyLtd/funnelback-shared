package com.funnelback.publicui.search.web.filters.utils;

import javax.servlet.http.HttpServletRequest;

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
     * @param request
     * @return
     */
    public String getProfileAndViewId(HttpServletRequest request) {
        String profileId = request.getParameter(RequestParameters.PROFILE);
        if (profileId == null
                && request.getPathInfo() != null
                && request.getPathInfo().startsWith(ResourcesController.MAPPING_PATH)
                && request.getPathInfo().indexOf('/', ResourcesController.MAPPING_PATH.length()) > -1) {
            Integer collectionStart = request.getPathInfo().indexOf('/', ResourcesController.MAPPING_PATH.length());
            
            profileId = request.getPathInfo().substring(
                    collectionStart + 1,
                    request.getPathInfo().indexOf('/', collectionStart + 1));
        }
        
        return profileId;
    }

}
