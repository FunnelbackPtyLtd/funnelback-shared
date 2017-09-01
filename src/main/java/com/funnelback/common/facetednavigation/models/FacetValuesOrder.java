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
    CATEGORY_DEFINITION_ORDER,
    
    /**
     * Values are sorted by the first number found in the label, in increasing
     * value.
     * for example $0.1 will be sorted before $2.2
     */
    LABEL_AS_NUMBER_ASCENDING,
    
    /**
     * Values are sorted by the first number found in the label, in decreasing
     * value.
     * For example $2.2 will be sorted before $0.1
     */
    LABEL_AS_NUMBER_DESCENDING,
    
    /**
     * Sort values produced by the Date facet category in ascending order.
     * For example yesterday is sorted before today.  
     * Special categories like Past 6 weeks or Next year will always 
     * be displayed before specific years like 2013.
     */
    DATE_ASCENDING,
    
    /**
     * Sort values produced by the Date facet category in descending order.
     * For example today is sorted before yesterday.  
     * 
     */
    DATE_DESCENDING,
    
    /**
     * Uses a custom comparator that can be set on the Facet. 
     */
    CUSTOM_COMPARATOR;
    
    ;
}
