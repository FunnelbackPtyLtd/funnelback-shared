package com.funnelback.publicui.search.web.controllers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.DateFieldFill;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.GScopeItem;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.MetadataFieldFill;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.QueryItem;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.service.resource.impl.FacetedNavigationConfigResource;
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
            SearchQuestion question,
            @ModelAttribute SearchUser user,
            String type) {
        
        customiseRequest(question, request);
        
        ModelAndView mav =
            searchController.search(request, response, question, user);
        
        Map<String, Object> model = mav.getModel();
        
        SearchResponse sr = (SearchResponse) model.get("response");
        
        if (!sr.hasResultPacket()) {
            throw new ContentAuditorException("Expected result packet for request, but got none");
        }
                
        sr.getCustomData().put("displayMetadata", readMetadataInfo(question, Keys.ModernUI.ContentAuditor.DISPLAY_METADATA));
        
        String viewName = getViewName(type);
        
        if (type != null && type.equals("csv_export")) {
            response.setContentType("text/csv");
            response.setHeader("content-disposition", "attachment; filename=export.csv");
        }
        
        return new ModelAndView(viewName, model);
    }

    /** Modify the given question to suit content auditor's needs 
     * @param request */
    private void customiseRequest(SearchQuestion question, HttpServletRequest request) {
        Config config = question.getCollection().getConfiguration();
        
        // We want search result links to come back to us
        // But still allow config to make it absolute
        String searchLink = config.value(Keys.ModernUI.SEARCH_LINK);
        searchLink = searchLink.replace("search.html", "content-auditor.html");
        config.setValue(Keys.ModernUI.SEARCH_LINK, searchLink);
        
        // Manipulate the request to suit content auditor
        question.getRawInputParameters().put(RequestParameters.STEM, new String[] {"0"});
        question.getRawInputParameters().put(RequestParameters.NUM_RANKS, new String[] {config.value(Keys.ModernUI.ContentAuditor.NUM_RANKS, "999")});
        question.getRawInputParameters().put(RequestParameters.DAAT, new String[] {"10000000"}); // 10m is the max according to http://docs.funnelback.com/14.0/query_processor_options_collection_cfg.html
        question.getDynamicQueryProcessorOptions().add("-daat_timeout=3600.0"); // 1 hour - Hopefully excessive
        
        if (request.getParameter("duplicate_signature") != null) {
            question.getRawInputParameters().put(RequestParameters.S, 
                ArrayUtils.add(
                    question.getRawInputParameters().get(RequestParameters.S),
                    request.getParameter("duplicate_signature")
                )
            );
            question.getRawInputParameters().put(RequestParameters.FULL_MATCHES_ONLY, new String[] {"on"});
            question.getRawInputParameters().put(RequestParameters.COLLAPSING, new String[] {"off"});
        }
        
        // Metadata for displaying in the results view
        question.getRawInputParameters().put("SM", new String[] {"meta"});
        StringBuilder sfValue = new StringBuilder();
        for (Map.Entry<String, String> entry : readMetadataInfo(question, Keys.ModernUI.ContentAuditor.DISPLAY_METADATA).entrySet()) {
            sfValue.append("," + entry.getKey());
        }
        question.getRawInputParameters().put("SF", new String[] {sfValue.toString()});

        if (!question.getRawInputParameters().containsKey(RequestParameters.COLLAPSING)) {
            question.getRawInputParameters().put(RequestParameters.COLLAPSING, new String[] {"on"});
            question.getRawInputParameters().put(RequestParameters.COLLAPSING_SIGNATURE, new String[] {question.getCollection().getConfiguration().value(Keys.ModernUI.ContentAuditor.COLLAPSING_SIGNATURE)});
        }

        if (question.getQuery() == null || question.getQuery().length() < 1) {
            question.setQuery("-padrenullquery");
        }

        question.getCollection().setFacetedNavigationLiveConfig(buildFacetConfig(question));
    }

    /** Resource manger for reading (and caching) config files */
    @Autowired
    @Setter
    protected ResourceManager resourceManager;

    /** Parser for faceted_navigation.xml */
    @Autowired
    @Setter
    private FacetedNavigationConfigParser fnConfigParser;

    /** Overwrite the facet config with a custom one */
    private FacetedNavigationConfig buildFacetConfig(SearchQuestion question) {

        List<FacetDefinition> facetDefinitions = new ArrayList<FacetDefinition>();
        
        facetDefinitions.add(createUrlFacetDefinition("URI"));

        facetDefinitions.add(createDateFacetDefinition("Date modified"));

        StringBuilder rmcfValue = new StringBuilder();
        for (Map.Entry<String, String> entry : readMetadataInfo(question, Keys.ModernUI.ContentAuditor.FACET_METADATA).entrySet()) {
            facetDefinitions.add(createMetadataFacetDefinition(entry.getValue(), entry.getKey()));
            rmcfValue.append("," + entry.getKey());
        }
        
        String qpOptions = " -rmcf="+rmcfValue+" -count_dates=d -count_urls=1000 -countgbits=all";

        // Pull in any query based facets from the index's faceted_navigation.xml file
        String indexView = question.getInputParameterMap().get("view");
        if (indexView == null) {
            indexView = "live";
        }

        // Read the snapshot's faceted nav config and get any gscope based facets
        File fnConfig = new File(question.getCollection().getConfiguration().getCollectionRoot(), indexView
            + File.separator + DefaultValues.FOLDER_IDX + File.separator
            + Files.FACETED_NAVIGATION_LIVE_CONFIG_FILENAME);
        try {
            FacetedNavigationConfig base = resourceManager.load(new FacetedNavigationConfigResource(fnConfig, fnConfigParser));
            if (base != null) {
                for (FacetDefinition fd : base.getFacetDefinitions()) {
                    for (CategoryDefinition cd : fd.getCategoryDefinitions()) {
                        if (cd instanceof GScopeItem || cd instanceof QueryItem) {
                            facetDefinitions.add(fd);
                        }
                        break;
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        FacetedNavigationConfig facetedNavigationConfig = new FacetedNavigationConfig(qpOptions, facetDefinitions);
        return facetedNavigationConfig;
    }

    /** 
     * Read all the metadata config settings for a prefix. The are expected to be in the form...
     * 
     * (prefix).(metadataClassName)=(label)
     * 
     * and will be returned as a hash of className to label
     */
    private Map<String, String> readMetadataInfo(SearchQuestion question, String keyPrefix) {
        // Read in the configured metadata classes, sort by keys
        Map<String, String> metadataClassToLabel = new HashMap<>();

        Config config = question.getCollection().getConfiguration();
        for (String key : question.getCollection().getConfiguration().valueKeys()) {
            if (key.startsWith(keyPrefix)) {
                String className = key.substring(keyPrefix.length() + 1 /* Skip the '.' */);
                String label = config.value(key);
                
                if (label.length() > 0) {
                    metadataClassToLabel.put(className, label);
                }
            }
        }
        return MapUtil.sortByValue(metadataClassToLabel);
    }

    /**
     * Creates a date based facet definition with the given label
     */
    private FacetDefinition createDateFacetDefinition(String label) {
        List<CategoryDefinition> categoryDefinitions = new ArrayList<CategoryDefinition>();
        DateFieldFill fill = new DateFieldFill();
        fill.setData("d");
        fill.setLabel(label);
        fill.setFacetName(label);
        categoryDefinitions.add(fill);
        
        return new FacetDefinition(label, categoryDefinitions);
    }

    /** Creates a URL scope based facet definition */
    private FacetDefinition createUrlFacetDefinition(String label) {
        List<CategoryDefinition> categoryDefinitions = new ArrayList<CategoryDefinition>();
        
        UrlScopeFill fill = new UrlScopeFill();
        fill.setLabel(label);
        fill.setFacetName(label);
        categoryDefinitions.add(fill);
        
        FacetDefinition result = new FacetDefinition(label, categoryDefinitions);
        return result;
    }

    /**
     * Creates a metadata field based facet definition with the given label, populated from the given metadataClass
     */
    private FacetDefinition createMetadataFacetDefinition(String label, String metadataClass) {
        List<CategoryDefinition> categoryDefinitions = new ArrayList<CategoryDefinition>();
        MetadataFieldFill fill = new MetadataFieldFill();
        fill.setData(metadataClass);
        fill.setLabel(label);
        fill.setFacetName(label);
        categoryDefinitions.add(fill);
        
        MetadataMissingFill remainder = new MetadataMissingFill();
        remainder.setData(metadataClass);
        remainder.setLabel(label);
        remainder.setFacetName(label);
        categoryDefinitions.add(remainder);
        
        return new FacetDefinition(label, categoryDefinitions);
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
