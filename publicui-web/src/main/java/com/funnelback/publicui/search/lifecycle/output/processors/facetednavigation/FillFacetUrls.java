package com.funnelback.publicui.search.lifecycle.output.processors.facetednavigation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.utils.FacetedNavigationUtils;
import com.funnelback.publicui.utils.QueryStringUtils;

import com.google.common.collect.ImmutableList;
import static com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters.FACET_PREFIX;
/**
 * Adds the UnselectALLUrl to the given facet.
 *
 */
public class FillFacetUrls {
    
    /**
     * List of the parameters to remove from URLs generated for facets. For example we don't
     * want start_rank to be preserved as we should go back to the first page when a facet
     * is selected
     */
    private final List<String> PARAMETERS_TO_REMOVE = ImmutableList.of("start_rank", "duplicate_start_rank");
    
    /**
     * Removes the PARAMETERS_TO_REMOVE from the URL question.
     * 
     * @param urlQuestion
     */
    public void removeParameters(Map<String, List<String>> urlQuestion) {
        PARAMETERS_TO_REMOVE.stream()
            .forEach(paramName -> FacetedNavigationUtils.removeQueryStringFacetKey(urlQuestion, paramName));
    }

    /**
     * Sets the UnselectyAllUrl to the given facet based on the given search transaction.
     * 
     * <p>The URL is based on the current QueryStringMap</p>
     * @param facet
     * @param st
     */
    public void setUnselectAllUrl(Facet facet, SearchTransaction st) {
        Map<String, List<String>> urlQuestion = st.getQuestion().getQueryStringMapCopy();
        unselectFacet(urlQuestion, facet);
        removeParameters(urlQuestion);
        
        facet.setUnselectAllUrl(QueryStringUtils.toString(urlQuestion, true));
    }
    
    public void unselectFacet(Map<String, List<String>> urlQuestion, FacetDefinition facet) {
        unslectFacet(urlQuestion, facet.getName());
    }
    
    public void unselectFacet(Map<String, List<String>> urlQuestion, Facet facet) {
        unslectFacet(urlQuestion, facet.getName());
    }
    
    public void unslectFacet(Map<String, List<String>> urlQuestion, String facetName) {
        FacetedNavigationUtils.editQueryStringFacetValue(urlQuestion, 
            m -> {
                    m.keySet().stream().filter(k -> k.startsWith(FACET_PREFIX + facetName + "|"))
                    .collect(Collectors.toList()).stream() // Convert back to a List so that we 
                                                           // don't modify the map while iterating over it.
                    .forEach(m::remove);
                });
    }
    
    public String unselectAllFacetsUrl(SearchTransaction st) {
        Map<String, List<String>> urlQuestion = st.getQuestion().getQueryStringMapCopy();
        unselectAllFacets(urlQuestion);
        return QueryStringUtils.toString(urlQuestion, true);
    }
    
    public void unselectAllFacets(Map<String, List<String>> urlQuestion) {
        FacetedNavigationUtils.editQueryStringFacetValue(urlQuestion, 
            m -> {
                    m.keySet().stream().filter(k -> k.startsWith(FACET_PREFIX) && k.contains("|"))
                    .collect(Collectors.toList()).stream() // Convert back to a List so that we 
                                                           // don't modify the map while iterating over it.
                    .forEach(m::remove);
                });
    }
}
