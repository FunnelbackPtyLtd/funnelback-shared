package com.funnelback.publicui.search.web.filters.utils;

import java.util.Optional;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.web.controllers.ResourcesController;
import com.funnelback.springmvc.utils.web.ServletFilterRequestParameterValueExtractor;

/**
 * PublicUI specific ServletFilterRequestParameterValueExtractor - Checks both URL params and web resources path params
 */
public class PublicUiServletFilterRequestParameterValueExtractor implements ServletFilterRequestParameterValueExtractor {

    /**
     * Checks both URL params and web resources path params
     */
    @Override
    public Optional<String> getCollectionValue(ServletRequest servletRequest) {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String collectionId = request.getParameter(RequestParameters.COLLECTION);
        if (collectionId == null
                && request.getPathInfo() != null
                && request.getPathInfo().startsWith(ResourcesController.MAPPING_PATH)
                && request.getPathInfo().indexOf('/', ResourcesController.MAPPING_PATH.length()) > -1) {
            collectionId = request.getPathInfo().substring(
                    ResourcesController.MAPPING_PATH.length(),
                    request.getPathInfo().indexOf('/', ResourcesController.MAPPING_PATH.length()));
        }
        
        return Optional.of(collectionId);
    }

}
