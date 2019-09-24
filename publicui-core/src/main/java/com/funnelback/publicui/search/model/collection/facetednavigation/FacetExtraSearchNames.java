package com.funnelback.publicui.search.model.collection.facetednavigation;

import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;

public class FacetExtraSearchNames {
    
    private static final String FACET_EXTRA_SEARCH_PREFIX = "INTERNAL_FACETED_NAV_SEARCH_";
    
    public static final String SEARCH_FOR_UNSCOPED_VALUES = FACET_EXTRA_SEARCH_PREFIX + "UNSCOPED_VALUES";
    
    public static final String SEARCH_FOR_ALL_VALUES = FACET_EXTRA_SEARCH_PREFIX + "ALL_VALUES";
    
    public static final String SEARCH_WHERE_FACET_IS_DISABLED = FACET_EXTRA_SEARCH_PREFIX + "FACET_DISABLED";
    
    private static final String SEP = "-";
    

    public String extraSearchToCalculateCounOfCategoryValue(Facet facet, CategoryValue value) {
        return getExtraSearchName(facet.getName(), value.getQueryStringParamName(), value.getData());
    }
    
    public String extraSearchToCalculateCounOfCategoryValue(FacetDefinition facet, CategoryDefinition catDef, String value) {
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
    String getExtraSearchName(String facetName, String queryStringParamName, String value) {
        // Base 64 the param and value to ensure we accidently don't generate non unique keys
        // for different inputs.
        // A seperator that wont appear in base64.
        return FACET_EXTRA_SEARCH_PREFIX 
            + encode(facetName) + SEP 
            + encode(queryStringParamName)+ SEP 
            + encode(value);
    }
    
    public String extraSearchWithFacetUnchecked(Facet facet) {
        return extraSearchWithFacetUnchecked(facet.getName());
    }
    
    public String extraSearchWithFacetUnchecked(FacetDefinition facet) {
        return extraSearchWithFacetUnchecked(facet.getName());
    }
    
    private String extraSearchWithFacetUnchecked(String facetName) {
        return SEARCH_WHERE_FACET_IS_DISABLED + SEP + encode(facetName);
    }
    
    private String encode(String s) {
        // Encode the value.
        // The escape char is \
        // The value is wrapped in " char
        // and so the only chars we need to escape are \ and "
        return "\"" + s.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            + "\"";
    }
    
    /**
     * Is the extra search a extra search for faceted navigation?
     *  
     * @param extraSearchName
     * @return
     */
    public boolean isFacetExtraSearch(String extraSearchName) {
        return extraSearchName.startsWith(FACET_EXTRA_SEARCH_PREFIX);
    }
}
