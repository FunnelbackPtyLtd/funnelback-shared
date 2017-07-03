package com.funnelback.publicui.search.lifecycle.output.processors.facetednavigation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.utils.FacetedNavigationUtils;
import com.funnelback.publicui.utils.QueryStringUtils;

/**
 * Adds the UnselectALLUrl to the given facet.
 *
 */
public class FillFacetUrls {

    /**
     * Sets the UnselectyAllUrl to the given facet based on the given search transaction.
     * 
     * <p>The URL is based on the current QueryStringMap</p>
     * @param facet
     * @param st
     */
    public void setUnselectAllUrl(Facet facet, SearchTransaction st) {
        Map<String, List<String>> qs = st.getQuestion().getQueryStringMapCopy();
        FacetedNavigationUtils.editQueryStringFacetValue(qs, 
            m -> {
                    m.keySet().stream().filter(k -> k.startsWith("f." + facet.getName() + "|"))
                    .collect(Collectors.toList()).stream() // Convert back to a List so that we 
                                                           // don't modify the map while iterating over it.
                    .forEach(m::remove);
                });
        facet.setUnselectAllUrl(QueryStringUtils.toString(qs, true));
    }
}
