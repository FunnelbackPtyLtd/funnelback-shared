package com.funnelback.publicui.search.web.controllers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Files;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.contentauditor.MapUtil;
import com.funnelback.publicui.contentauditor.MetadataMissingFill;
import com.funnelback.publicui.contentauditor.UrlScopeFill;
import com.funnelback.publicui.contentauditor.YearOnlyDateFieldFill;
import com.funnelback.publicui.search.lifecycle.SearchTransactionProcessor;
import com.funnelback.publicui.search.lifecycle.input.processors.extrasearches.FacetedNavigationQuestionFactory;
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.DateFieldFill;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.GScopeItem;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.MetadataFieldFill;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.QueryItem;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.SearchQuestionType;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.service.resource.impl.FacetedNavigationConfigResource;
import com.funnelback.publicui.search.web.binding.SearchQuestionBinder;
import com.funnelback.publicui.search.web.controllers.SearchController.ModelAttributes;
import com.funnelback.publicui.xml.FacetedNavigationConfigParser;
import com.funnelback.springmvc.service.resource.ResourceManager;

/**
 * Presents a content auditor interface.
 * 
 * Expects the same set of parameters as SerchController (which this uses internally), however some
 * search settings are overridden (e.g. the requested form is always ignored).
 */
@Controller
@Log4j
public class ContentAuditorController {

    /**
     * Represents an unexpected error in the Content Auditor system
     */
    public class ContentAuditorException extends RuntimeException {
        
        /** ID for serialization */
        private static final long serialVersionUID = 1L;

        /** Construct with a message */
        public ContentAuditorException(String message) {
            super(message);
        }
    }

    /**
     * SearchController is used to perform the actual search requests to create the auditor report
     */
    @Autowired
    private SearchController searchController;

    /**
     * We apply the SearchController's initBinder (since we want to replicate how it handles requests).
     */
    @InitBinder
    public void initBinder(DataBinder binder) {
        searchController.initBinder(binder);
    }
    
    /**
     * Main entry point for Content Auditor
     * 
     * This method produces a content auditing report, using its
     * SearchController to get the necessary raw data.
     */
    @RequestMapping("/content-auditor.*")
    public ModelAndView generateContentAuditorReport(
            HttpServletRequest request,
            HttpServletResponse response,
            SearchQuestion originalQuestion,
            @ModelAttribute SearchUser user,
            String type) {
        
        originalQuestion.setQuestionType(SearchQuestionType.CONTENT_AUDITOR);

        ModelAndView mav =
            searchController.search(request, response, originalQuestion, user);

        SearchResponse sr = (SearchResponse) mav.getModel().get(SearchController.ModelAttributes.response.toString());
        
        if (sr == null || !sr.hasResultPacket()) {
            throw new ContentAuditorException("Expected result packet for request, but got none");
        }
                
        String viewName = getViewName(type);
        
        if (type != null && type.equals("csv_export")) {
            response.setContentType("text/csv");
            response.setHeader("content-disposition", "attachment; filename=export.csv");
        }
        
        return new ModelAndView(viewName, mav.getModel());
    }

    /** Constructs the path to the requested content auditor view (i.e. freemarker ftl file) */
    private String getViewName(String type) {
        if (type == null) {
            type = "index";
        }
        
        // Content auditor always uses the specific content auditor templates, not collection ones
        String viewName = 
            DefaultValues.FOLDER_WEB + "/" +
            DefaultValues.FOLDER_TEMPLATES + "/" +
            DefaultValues.FOLDER_MODERNUI + "/" +
            DefaultValues.FOLDER_CONTENT_AUDITOR + "/" +
            type;
        return viewName;
    }

}
