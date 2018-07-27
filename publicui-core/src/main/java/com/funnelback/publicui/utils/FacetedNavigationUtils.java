package com.funnelback.publicui.utils;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import com.funnelback.common.function.StreamUtils;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.collection.Profile;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.facetednavigation.FacetParameter;
import com.funnelback.publicui.search.model.facetednavigation.FacetSelectedDetails;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

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
    
    public static FacetedNavigationConfig selectConfiguration(SearchTransaction st) {
        return selectConfiguration(st.getQuestion().getCollection(), st.getQuestion().getProfile());
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
    
    public static List<FacetSelectedDetails> getFacetSelectedDetails(SearchQuestion searchQuestion) {
        return getFacetParameters(searchQuestion)
            .stream()
            .map(f -> StreamUtils.ofNullable(f.getValues())
                    .map(v -> new FacetSelectedDetails(f.getName(), f.getExtraParameter(), v)))
            .flatMap(i -> i)
            .filter(f -> !"".equals(f.getValue())) // Skip empty values.
            .collect(Collectors.toList());
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
    
    /**
     * Checks if the facet that the category value belongs to is a facet that is selected.
     * 
     * <p>The facet is is selected if any category value under the facet is selected.</p>
     * @param cDef
     * @param selectedCategories
     * @return
     */
    public static boolean isFacetSelected(CategoryDefinition cDef, Map<String, List<String>> selectedCategories) {
        String keyPrefix = facetParamNamePrefix(cDef.getFacetName());
        return selectedCategories.keySet().stream().anyMatch(k -> k.startsWith(keyPrefix));
    }
    
    /**
     * Checks if the facet that the category value belongs to is a facet that is selected.
     * 
     * <p>The facet is is selected if any category value under the facet is selected.</p>
     * @param facetDefinition
     * @param selectedCategories
     * @return
     */
    public static boolean isFacetSelected(FacetDefinition facetDefinition, Map<String, List<String>> selectedCategories) {
        String keyPrefix = facetParamNamePrefix(facetDefinition.getName());
        return selectedCategories.keySet().stream().anyMatch(k -> k.startsWith(keyPrefix));
    }
    
    /**
     * Gets the prefix of a faceted navigation category value prefix.
     * 
     *  <p>This is prefix of the key that is added to the URL if the category
     *  value is selected e.g. f.nameOfFacet|</p>
     * @param nameOfFacet
     * @return
     */
    public static String facetParamNamePrefix(String nameOfFacet) {
        return RequestParameters.FACET_PREFIX + nameOfFacet + CategoryDefinition.QS_PARAM_SEPARATOR;
    }
    
    /**
     * Remove the query key value pair from the queryStringMap looking in the facetScope param as well.
     * 
     * <p>If no values are left over for a key, the key will be removed from the map as well.<p>
     * 
     * @param queryStringMap The query section of the URL as a Map (see {@link SearchQuestion#getQueryStringMapCopy()}, 
     * which will have the paramName=paramValue removed.
     * @param paramName Name of the parameter to remove the value from
     * @param paramValue Value to remove
     */
    public static void removeQueryStringFacetValue(Map<String, List<String>> queryStringMap, String paramName, String paramValue) {
        editQueryStringFacetValue(queryStringMap, map -> removeValueAndPossiblyParameterFromMap(map, paramName, paramValue));
    }
    
    /**
     * Remove from the query any key value pair with a specific key from the queryStringMap looking in the facetScope param as well.
     * 
     * 
     * @param queryStringMap The query section of the URL as a Map (see {@link SearchQuestion#getQueryStringMapCopy()}
     * @param paramName Name of the parameter to remove the value from.
     */
    public static void removeQueryStringFacetKey(Map<String, List<String>> queryStringMap, String paramName) {
        editQueryStringFacetValue(queryStringMap, map -> map.remove(paramName));
    }

    /**
     * <p>Remove a value from a Map, and possibly the key as well if the value to remove was the only one</p>
     *
     * @param queryStringMap Map to remove the value from
     * @param paramName Name of the parameter to remove the value from
     * @param paramValue Value to remove
     */
    private static void removeValueAndPossiblyParameterFromMap(Map<String, List<String>> queryStringMap, String paramName, String paramValue) {
        if (queryStringMap.containsKey(paramName)) {
            // Remove the value if there's one. There may be multiple values for the
            // same name, when multiple values are selected (checkboxes)
            queryStringMap.get(paramName).remove(paramValue);
            if (queryStringMap.get(paramName).isEmpty()) {
                // If it was the only value, remove the parameter entirely
                queryStringMap.remove(paramName);
            }
        }
    }
    
    /**
     * Applies the modifier to the given map and if the facetScope is present creates a Map from that and passes it to the modifier.
     * 
     * <p>This is usefil when working on facets as the options map be set directly on the map or within the facetScope value.</p>
     * @param queryStringMap
     * @param modifier
     */
    public static void editQueryStringFacetValue(Map<String, List<String>> queryStringMap, Consumer<Map<String, List<String>>> modifier) {
        modifier.accept(queryStringMap);

        // Also remove value from the facetScope if it exists
        if (queryStringMap.containsKey(SearchQuestion.RequestParameters.FACET_SCOPE)) {
            // Assume facetScope has only 1 value
            Map<String, List<String>> facetScopeQs = QueryStringUtils.toMap(queryStringMap
                .get(SearchQuestion.RequestParameters.FACET_SCOPE)
                .stream()
                .findFirst()
                .orElse(""));

            modifier.accept(facetScopeQs);

            // If no parameters remain in facetScope, just remove it from the query string
            if (facetScopeQs.isEmpty()) {
                queryStringMap.remove(SearchQuestion.RequestParameters.FACET_SCOPE);
            } else {
                if(log.isDebugEnabled()) {
                    log.debug("Transformaed map to be: {}", QueryStringUtils.listMapAsString(facetScopeQs));
                }
                // Serialize it back
                queryStringMap.put(SearchQuestion.RequestParameters.FACET_SCOPE, asList(QueryStringUtils.toString(facetScopeQs, false)));
            }
        }
    }
    
}
