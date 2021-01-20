package com.funnelback.common.facetednavigation.models;

/**
 * Defines how facet selection works.
 *
 */
public enum FacetSelectionType {
    /**
     * Unselects other facets when selected, useful for tab type facets.
     * 
     */
    SINGLE_AND_UNSELECT_OTHER_FACETS,
    /**
     * When a category value is selected other category values of equal or lower
     * depth are unselected.
     */
    SINGLE,
    
    /**
     * When a category value is selected other category values are left selected.
     */
    MULTIPLE;
}
