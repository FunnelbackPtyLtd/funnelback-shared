package com.funnelback.common.facetednavigation.models;

/**
 * Defines how selected constraints within a facet are applied.
 *
 */
public enum FacetConstraintJoin {
    /**
     * Requires that all selected constraints are meet.  
     */
    AND,
    /**
     * Requires that at least on of the selected constraints are meet.
     */
    OR,
    /**
     * Constraints are joined in the same way as version 15.10 and before did.
     */
    LEGACY;
}
