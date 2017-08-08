package com.funnelback.common.facetednavigation.models;

/**
 * Defines where the (category) values to display in facet come from.
 *
 */
public enum FacetValues {
    /**
     * Values are from the query which may have facets applied.
     * <p>If you had selected `red cars` then (if each car has one colour)
     * you would not see facet values for any other car colour.</p>
     */
    FROM_SCOPED_QUERY,
    
    /**
     * Values are from the unscoped query, that is the user's query without
     * any facets selected.
     * <p>If you had selected `red cars` then you would still see the colours of
     * other cars, as long as they also matched the user's query.</p>
     */
    FROM_UNSCOPED_QUERY,
    
    /**
     * Values are from running the all (padre null) query where no facets
     * are selected.
     * <p>This is what is needed for tabs where even if the user query only has matches
     * from one tab we would still show all tabs.</p>
     */
    FROM_UNSCOPED_ALL_QUERY;
}
