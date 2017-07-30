package com.funnelback.publicui.search.model.collection.facetednavigation;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;

public class FacetExtraSearchNames {
    
    private static final String FACET_EXTRA_SEARCH_PREFIX = "INTERNAL_FACETED_NAV_SEARCH_";
    

    public String getExtraSearchName(Facet facet, CategoryValue value) {
        return getExtraSearchName(facet.getName(), value.getQueryStringParamName(), value.getData());
    }
    
    public String getExtraSearchName(FacetDefinition facet, CategoryDefinition catDef, String value) {
        return getExtraSearchName(facet.getName(), catDef.getQueryStringParamName(), value);
    }
    
    /**
     * Create a extra search to work out the counts for specific facet, category and category value.
     * 
     * <p>Note this requires that the facet, category string param name are unique between facets</p>
     * 
     * @param facetName The name of the facet.
     * @param queryStringParamName the query string paramater name. This comes from the Category 
     * (assumed to be unique amongst the facet).
     * @param value The selected value within the Category, the search simulates this category 
     * value being picked.
     * @return
     */
    private String getExtraSearchName(String facetName, String queryStringParamName, String value) {
        // Base 64 the param and value to ensure we accidently don't generate non unique keys
        // for different inputs.
        // A seperator that wont appear in base64.
        String seperator = "-";
        return FACET_EXTRA_SEARCH_PREFIX 
            + encode(facetName) 
            + seperator +
            encode(queryStringParamName)
            + seperator + 
            encode(value);
    }
    
    private String encode(String s) {
        return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }
}
