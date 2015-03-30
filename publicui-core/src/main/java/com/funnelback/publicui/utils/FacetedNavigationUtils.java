package com.funnelback.publicui.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import lombok.extern.log4j.Log4j;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.common.views.View;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.collection.Profile;
import com.funnelback.publicui.search.model.facetednavigation.FacetParameter;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;

@Log4j
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
        // Default config from the live directory
        FacetedNavigationConfig config = c.getFacetedNavigationLiveConfig();

        String configLocationOverride = null;
        
        if (c.getConfiguration() != null) {
            // ...possibly overriden in collection config
            configLocationOverride = c.getConfiguration().value(Keys.FacetedNavigation.CONFIG_LOCATION,
                    View.live.name());
            if (DefaultValues.FOLDER_CONF.equals(configLocationOverride)) {
                config = c.getFacetedNavigationConfConfig();
            }
        }
        
        // If we have no config at this point, we can look at profiles
        if (config == null && profileId != null) {
            Profile p = c.getProfiles().get(profileId);
            if (p != null) {
                // ...and at conf or live config depending of the override setting
                if (DefaultValues.FOLDER_CONF.equals(configLocationOverride)) {
                    config = p.getFacetedNavConfConfig();
                } else {
                    config = p.getFacetedNavLiveConfig();
                }
            }
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
    
}
