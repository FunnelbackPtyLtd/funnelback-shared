package com.funnelback.common.facetednavigation.models;

/**
 * Defines how selected constraints within a facet are applied.
 *
 */
public enum FacetConstraintJoin {
    /**
     * Requires that all selected constraints are met.  
     */
    AND,
    /**
     * Requires that at least one of the selected constraints are met.
     */
    OR,
    /**
     * Constraints are joined in the same way as version 15.10 and before did.
     */
    LEGACY;
}
