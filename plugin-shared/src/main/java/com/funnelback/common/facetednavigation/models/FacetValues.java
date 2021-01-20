package com.funnelback.common.facetednavigation.models;

/**
 * Defines where the (category) values to display in facet come from.
 *
 */
public enum FacetValues {
    /**
     * Values come from the scoped query however if a value
     * is selected all other non selected values at the same
     * category level are not shown.
     * e.g. If every document is in both Aus and NZ and we selected Aus
     * only the value Aus would be selected. If we configured facets
     * to show states under the country we would still see the following values:
     * 
     * (selected) Aus
     *   - NSW
     *   - ACT
     * 
     * Where if we just used FROM_SCOPED_QUERY we would see:
     * (selected) Aus
     * NZ
     *   - NSW
     *   - ACT
     * 
     */
    FROM_SCOPED_QUERY_HIDE_UNSELECTED_PARENT_VALUES,
    
    /**
     * Values are from the query which may have facets applied.
     * <p>If you had selected `red cars` then (if each car has one colour)
     * you would not see facet values for any other car colour.</p>
     */
    FROM_SCOPED_QUERY,
    
    /**
     * Values are from the query which may have facets applied, except 
     * the 'current' facet is not selected.
     * 
     * <p>For example if you had a tab facet that was selected and a radio
     * facet which had this option set then the values for the radio
     * facet would be from a query where the tab facet was selected
     * but the radio facet was unselected.</p>
     * 
     * <p>This is a good option for multi select OR and radio as it wont show
     * options that lead to zero result pages.</p>
     * 
     */
    FROM_SCOPED_QUERY_WITH_FACET_UNSELECTED,
    
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
