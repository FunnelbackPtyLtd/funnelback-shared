package com.funnelback.publicui.search.model.collection.facetednavigation;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;

public class ExtraSearchNames {

  //TODO this should take a cat val and should contain the value and the query param.
    public String getExtraSearchName(Facet facet, CategoryValue value) {
        return getExtraSearchName(facet.getName(), value.getQueryStringParamName(), value.getData());
    }
    
    public String getExtraSearchName(FacetDefinition facet, CategoryDefinition catDef, String value) {
        return getExtraSearchName(facet.getName(), catDef.getQueryStringParamName(), value);
    }
    
    private String getExtraSearchName(String facetName, String queryString, String value) {
        return "INTERNAL_FACETED_NAV_SEARCH_" + facetName + "--" + 
            Base64.getEncoder().encodeToString(queryString.getBytes(StandardCharsets.UTF_8))
            + "--" + 
            Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
}
