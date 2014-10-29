package com.funnelback.publicui.search.web.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.log4j.Log4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.DateFieldFill;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.MetadataFieldFill;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.URLFill;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;

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
            SearchQuestion question,
            @ModelAttribute SearchUser user,
            String type) {
        
        // We want search result links to come back to us
        // But still allow config to make it absolute
        String searchLink = question.getCollection().getConfiguration().value(Keys.ModernUI.SEARCH_LINK);
        searchLink = searchLink.replace("search.html", "content-auditor.html");
        question.getCollection().getConfiguration().setValue(Keys.ModernUI.SEARCH_LINK, searchLink);
        
        // TODO - Manipulate the request if we need to
        question.getRawInputParameters().put(RequestParameters.NUM_RANKS, new String[] {"999"});

        if (question.getQuery() == null) {
            question.setQuery("-padrenullquery");
        }
        
        // TODO - Source this form somewhere? A collection.cfg setting?
        Map<String, Character> metadataFields = new HashMap<String, Character>();
        metadataFields.put("Creator", 'a');
        metadataFields.put("Publisher", 'p');
        metadataFields.put("Subject", 's');
        metadataFields.put("Format", 'f');
        metadataFields.put("Generator", 'y');
        metadataFields.put("Page Type", '0');
        metadataFields.put("Business Stage", 'B');
        metadataFields.put("Business Structure", 'C');
        metadataFields.put("Four Pillars", 'D');
        metadataFields.put("Red Tape Reduction", 'E');

        // Overwrite the facet config with a custom one
        String qpOptions = " -rmcf="+constructRmcfValue(metadataFields)+" -count_dates=d -count_urls=1000";
        List<FacetDefinition> facetDefinitions = new ArrayList<FacetDefinition>();
        
        // TODO - Source the base URL from somewhere (a collection.cfg setting?)
        facetDefinitions.add(createPathFacetDefinition("Path", "http://www.business.qld.gov.au/"));
        facetDefinitions.add(createDateFacetDefinition("Date modified"));
        
        for (Map.Entry<String, Character> entry : metadataFields.entrySet()) {
            facetDefinitions.add(createMetadataFacetDefinition(entry.getKey(), entry.getValue()));            
        }

        /*
         * TODO - We need to somehow support detecting missing metadata, ideally without having
         * to set gscope bits or running extra queries.
         * 
         * The current implementation uses gscope bits set with something like...
         * 
         *     <QueryItem>
         *       <Data>Subject</Data>
         *       <Query>-s:$++ $++</Query>
         *     </QueryItem>
         */
        
        FacetedNavigationConfig facetedNavigationConfig = new FacetedNavigationConfig(qpOptions, facetDefinitions);
        
        question.getCollection().setFacetedNavigationLiveConfig(facetedNavigationConfig);
        
        ModelAndView mav =
            searchController.search(request, response, question, user);
        
        Map<String, Object> model = mav.getModel();

        // Content auditor always uses the specific content auditor template
        
        if (type == null) {
            type = "index";
        }
        
        String viewName = 
            DefaultValues.FOLDER_WEB + "/" +
            DefaultValues.FOLDER_TEMPLATES + "/" +
            DefaultValues.FOLDER_MODERNUI + "/" +
            DefaultValues.FOLDER_CONTENT_AUDITOR + "/" +
            type;
        
        return new ModelAndView(viewName, model);
    }

    /**
     * Return an rmcf query processor option value covering all the given metadata fields
     */
    private String constructRmcfValue(Map<String, Character> metadataFields) {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, Character> entry : metadataFields.entrySet()) {
            result.append(entry.getValue());
        }
        
        return result.toString();
    }

    /**
     * Creates a date based facet definition with the given label
     */
    private FacetDefinition createDateFacetDefinition(String label) {
        List<CategoryDefinition> categoryDefinitions = new ArrayList<CategoryDefinition>();
        DateFieldFill fill = new DateFieldFill();
        fill.setData("d");
        categoryDefinitions.add(fill);
        
        return new FacetDefinition(label, categoryDefinitions);
    }

    /**
     * Creates a URL path based facet definition with the given label, starting from urlBase
     */
    private FacetDefinition createPathFacetDefinition(String label, String urlBase) {
        List<CategoryDefinition> categoryDefinitions = new ArrayList<CategoryDefinition>();
        URLFill fill = new URLFill();
        fill.setData(urlBase);
        categoryDefinitions.add(fill);
        
        return new FacetDefinition(label, categoryDefinitions);
    }

    /**
     * Creates a metadata field based facet definition with the given label, populated from the given metadataClass
     */
    private FacetDefinition createMetadataFacetDefinition(String Label, Character character) {
        List<CategoryDefinition> categoryDefinitions = new ArrayList<CategoryDefinition>();
        MetadataFieldFill fill = new MetadataFieldFill();
        fill.setData(character.toString());
        categoryDefinitions.add(fill);
        
        return new FacetDefinition(Label, categoryDefinitions);
    }

}
