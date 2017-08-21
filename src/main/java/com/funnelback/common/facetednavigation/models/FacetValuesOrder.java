package com.funnelback.common.facetednavigation.models;

public enum FacetValuesOrder {
    /**
     * Values should be sorted by the count of documents returned
     * when the category value is selected. Where the highest count is 
     * first.
     */
    COUNT_DESCENDING,
    
    /**
     * Values should be sorted by the count of documents returned
     * when the category value is selected. Where the lowest count is 
     * first.
     */
    COUNT_ASCENDING,
    
    /**
     * Values that have been selected are sorted before those that have not
     * been selected.
     */
    SELECTED_FIRST,
    
    /**
     * Sort the values by lexicographically comparing the lowercase equivalent 
     * of the label, with lower values first e.g. a before b.
     */
    LABEL_ASCENDING,
    
    /**
     * Sort the values by lexicographically comparing the lowercase equivalent 
     * of the label, with higher values first e.g. b before a.
     */
    LABEL_DESCENDING,
    
    /**
     * Values should be in the same order as the category definitions are
     * in the facet configuration.
     */
    CATEGORY_DEFINITION_ORDER;
}
