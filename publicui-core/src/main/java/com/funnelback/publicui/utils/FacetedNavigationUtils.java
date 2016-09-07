package com.funnelback.publicui.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.collection.Profile;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.facetednavigation.FacetParameter;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class FacetedNavigationUtils {

    /**
     * Will select the correct faceted navigation configuration depending on
     * collection parameters (Is there a config location override ?) and the presence
     * of a profile.
     * @param c
     * @param p
     * @return
     */
    public static FacetedNavigationConfig selectConfiguration(Collection c, String profileId) {
        FacetedNavigationConfig config = null;
        if (profileId != null) {
            Profile p = c.getProfiles().get(profileId);
            if (p != null) {
                config = p.getFacetedNavConfConfig();
            }
        }
        
        //If the profile does not have faceted nav look in live.
        if(config == null) {
            config = c.getFacetedNavigationLiveConfig();
        }
        
        
        //It may not be copied to live if the step is skipped or maybe the file is deleted, 
        //use the collection level conf config
        if(config == null) {
            return c.getFacetedNavigationConfConfig();
        }
        
        return config;
    }
    
    /**
     * Returns a set of facet parameters (i.e. CGI parameters for facet selection)
     * which exist within the SearchQuestion.
     */
    public static List<FacetParameter> getFacetParameters(SearchQuestion searchQuestion) {
        List<FacetParameter> result = new ArrayList<>();
        
        // Read facet names from the 'raw' parameters since they can
        // be multi-valued (Multiple categories selected for a single facet)
        MapKeyFilter filter = new MapKeyFilter(searchQuestion.getRawInputParameters());
        String[] selectedFacetsParams = filter.filter(RequestParameters.FACET_PARAM_PATTERN);
        
        if (selectedFacetsParams.length > 0) {
            for (final String selectedFacetParam: selectedFacetsParams) {
                log.debug("Found facet parameter '" + selectedFacetParam + "'");
                Matcher m = RequestParameters.FACET_PARAM_PATTERN.matcher(selectedFacetParam);
                m.find();
                
                final String facetName = m.group(1);
                final String extraParam = m.group(3);
                log.debug("Found facet name '" + facetName + "' and extra parameter '" + extraParam + "'");
                
                final String values[] = searchQuestion.getRawInputParameters().get(selectedFacetParam);
                
                result.add(new FacetParameter(facetName, extraParam, values));
            }
        }
        
        return result;
    }
    
    /**
     * Check if a category value is currently selected
     * @param cDef Category definition to check
     * @param selectedCategories List of selected categories from the transaction
     * @param categoryValue Value to check for selection
     * @return
     */
    public static boolean isCategorySelected(CategoryDefinition cDef, Map<String, List<String>> selectedCategories, String categoryValue) {
        return selectedCategories
            .entrySet()
            .stream()
            .filter(entry -> entry.getKey().equals(cDef.getQueryStringParamName()))
            .anyMatch(entry -> entry.getValue().contains(categoryValue));
    }
    
}
