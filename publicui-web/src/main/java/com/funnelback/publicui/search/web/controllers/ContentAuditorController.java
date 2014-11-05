package com.funnelback.publicui.search.web.controllers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.SneakyThrows;
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
import com.funnelback.publicui.search.model.collection.facetednavigation.MetadataBasedCategory;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition.MetadataAndValue;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.DateFieldFill;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.MetadataFieldFill;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.URLFill;
import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.Facet.Category;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
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
     * A custom metadata fill which is used to capture the 'remainder' documents (i.e. those which did not have any of the 
     * desired type of metadata).
     * 
     * Note that the query which will be run by selecting this relies on -ifb having been used as an indexer option.
     */
    public class MetadataRemainderFill extends MetadataFieldFill {
        
        /** The category label shown for the remainder facet */
        private static final String CATEGORY_LABEL = "None";
        
        /** Special value shown as the param value (though this is not actually used) */
        private static final String UNSET_VALUE = "$unset";
        
        /**
         * Works just like MetadataFieldFill's setData, but always prefixes a '-'
         * to distinguish it from the MetadataFieldFill one.
         */
        @Override
        public void setData(String data) {
            this.data = "-" + data;
        }
        
        /** 
         * Produces only one categoryValue, which will have a count of the remaining documents
         * (i.e. those for which no metadata in this class was set) which will select those documents.
         */
        @Override
        @SneakyThrows(UnsupportedEncodingException.class)
        public List<CategoryValue> computeValues(final SearchTransaction st) {
            int remainderCount = st.getResponse().getResultPacket().getResultsSummary().getTotalMatching();
            
            // Subtract out the metadata entries which have been assigned
            for (Entry<String, Integer> entry : st.getResponse().getResultPacket().getRmcs().entrySet()) {
                String item = entry.getKey();
                int count = entry.getValue();
                MetadataAndValue mdv = parseMetadata(item);
                if (this.data.equals(mdv.metadata)) {
                    remainderCount -= count;
                }
            }
            List<CategoryValue> categories = new ArrayList<CategoryValue>();

            if (remainderCount > 0) {
                // There are some documents which didn't have any matches, so add a new remainder entry
                categories.add(new CategoryValue(
                    CATEGORY_LABEL,
                    CATEGORY_LABEL,
                    remainderCount,
                    URLEncoder.encode(getQueryStringParamName(), "UTF-8")
                    + "=" + URLEncoder.encode(UNSET_VALUE, "UTF-8"),
                    getMetadataClass()));
            }
            
            return categories;
        }

        /**
        * Produces a query constraint like -x:$++ (i.e. all documents where x was never set to anything).
        * 
        * Value is ignored (but should be expected to be UNSET_VALUE).
        */
        @Override
        public String getQueryConstraint(String value) {
            // This produces a query like -x:$++ (i.e. all documents where x was never set to anything)
            return data + ":" + MetadataBasedCategory.INDEX_FIELD_BOUNDARY;
        }
    }

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
        
        // We want search result links to come back to us
        // But still allow config to make it absolute
        String searchLink = question.getCollection().getConfiguration().value(Keys.ModernUI.SEARCH_LINK);
        searchLink = searchLink.replace("search.html", "content-auditor.html");
        question.getCollection().getConfiguration().setValue(Keys.ModernUI.SEARCH_LINK, searchLink);
        
        // Manipulate the request to suit content auditor
        question.getRawInputParameters().put(RequestParameters.STEM, new String[] {"0"});
        question.getRawInputParameters().put(RequestParameters.NUM_RANKS, new String[] {"999"});
        question.getRawInputParameters().put(RequestParameters.DAAT, new String[] {"10000000"}); // 10m is the max according to http://docs.funnelback.com/14.0/query_processor_options_collection_cfg.html
        question.getDynamicQueryProcessorOptions().add("-daat_timeout=3600.0"); // 1 hour - Hopefully excessive

        if (question.getQuery() == null || question.getQuery().length() < 1) {
            question.setQuery("-padrenullquery");
        }
        
        // Read in the configured metadata classes
        Map<String, Character> metadataFields = new HashMap<String, Character>();

        Config config = question.getCollection().getConfiguration();
        for (String key : question.getCollection().getConfiguration().valueKeys()) {
            if (key.startsWith(Keys.ModernUI.ContentAuditor.METADATA)) {
                String className = key.substring(Keys.ModernUI.ContentAuditor.METADATA.length() + 1 /* Skip the '.' */);
                String label = config.value(key);
                
                if (label.length() > 0) {
                    metadataFields.put(label, className.charAt(0));
                }
            }
        }

        // Overwrite the facet config with a custom one
        String qpOptions = " -rmcf="+constructRmcfValue(metadataFields)+" -count_dates=d -count_urls=1000";
        List<FacetDefinition> facetDefinitions = new ArrayList<FacetDefinition>();
        
        // TODO - Source the base URL from somewhere (a collection.cfg setting?)
        String scopingUrl = request.getParameter("scoping_url");
        if (scopingUrl != null) {
            facetDefinitions.add(createPathFacetDefinition("Path", scopingUrl));
            question.getInputParameterMap().put("scope", scopingUrl.replaceFirst("^" + Pattern.quote("http://"), ""));
        }
        facetDefinitions.add(createDateFacetDefinition("Date modified"));
        
        for (Map.Entry<String, Character> entry : metadataFields.entrySet()) {
            facetDefinitions.add(createMetadataFacetDefinition(entry.getKey(), entry.getValue()));
        }
        
        FacetedNavigationConfig facetedNavigationConfig = new FacetedNavigationConfig(qpOptions, facetDefinitions);
        
        question.getCollection().setFacetedNavigationLiveConfig(facetedNavigationConfig);
        
        ModelAndView mav =
            searchController.search(request, response, question, user);
                
        Map<String, Object> model = mav.getModel();
        
        SearchResponse sr = (SearchResponse) model.get("response");

        if (!sr.hasResultPacket()) {
            throw new ContentAuditorException("Expected result packet for request, but got none");
        }
        
//        Integer totalMatching = sr.getResultPacket().getResultsSummary().getTotalMatching();
//        
//        // Insert 'missing' values for where there was no metadata of the given class in a document
//        for (Facet f : sr.getFacets()) {
//            for (Category c : f.getCategories()) {
//                int withCategoryValueCount = 0;
//                for (CategoryValue cv : c.getValues()) {
//                    withCategoryValueCount += cv.getCount();
//                }
//                
//                if (withCategoryValueCount < totalMatching) {
//                    /*
//                     * TODO - We need to somehow make the links go somewhere useful - Maybe with a query like
//                     * -s:"$++ $++"
//                     */
//                    String metadataClass = Character.toString(c.getQueryStringParamName().charAt(c.getQueryStringParamName().length() - 1));
//                    c.getValues().add(new CategoryValue("None", "None", (totalMatching - withCategoryValueCount), "s=-" + metadataClass + ":\"$++ $++\"", metadataClass));
//                }
//            }
//            
//        }

        if (type == null) {
            type = "index";
        }
        
        // Content auditor always uses the specific content auditor template
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
        fill.setLabel(label);
        fill.setFacetName(label);
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
        fill.setLabel(label);
        fill.setFacetName(label);
        categoryDefinitions.add(fill);
        
        return new FacetDefinition(label, categoryDefinitions);
    }

    /**
     * Creates a metadata field based facet definition with the given label, populated from the given metadataClass
     */
    private FacetDefinition createMetadataFacetDefinition(String label, Character character) {
        List<CategoryDefinition> categoryDefinitions = new ArrayList<CategoryDefinition>();
        MetadataFieldFill fill = new MetadataFieldFill();
        fill.setData(character.toString());
        fill.setLabel(label);
        fill.setFacetName(label);
        categoryDefinitions.add(fill);
        
        MetadataRemainderFill remainder = new MetadataRemainderFill();
        remainder.setData("-" + character.toString());
        remainder.setLabel(label);
        remainder.setFacetName(label);
        categoryDefinitions.add(remainder);
        
        return new FacetDefinition(label, categoryDefinitions);
    }

}
