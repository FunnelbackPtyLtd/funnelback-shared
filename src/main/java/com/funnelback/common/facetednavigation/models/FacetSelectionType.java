package com.funnelback.common.facetednavigation.models;

/**
 * Defines how facet selection works.
 *
 */
public enum FacetSelectionType {
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
