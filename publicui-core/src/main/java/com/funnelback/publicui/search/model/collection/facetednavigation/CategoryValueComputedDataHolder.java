package com.funnelback.publicui.search.model.collection.facetednavigation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Holds the data of a category value.
 * 
 * <p>Unlike CategoryValue this has everything within it to generate a
 * CategoryValue without doubling up on any data in different forms.</p>
 * 
 * @since 15.14
 */
@AllArgsConstructor
public class CategoryValueComputedDataHolder {

    /** Actual value of the category (Ex: "Sydney").
     * 
     * @since 15.14
     */
    @Getter @Setter private String data;
    
    /** Label of the value, usually the same as the data.
     *  
     * @since 15.14
     */
    @Getter @Setter private String label;
    
    /** Count of occurrences for this value 
     * 
     * @since 15.14
     */
    @Getter @Setter private Integer count;
    
    /**
     * Constraint used to get this value. Can be a metadata class
     * or a GScope number, depending of the facet type.
     * 
     * @since 15.14
     */
    @Getter @Setter private String constraint;
    
    /**
     * Indicates if this value is currently selected
     * 
     * @since 15.14
     */
    @Getter @Setter private boolean selected;
    
    /** Name of the query string parameter for this value (e.g. <code>f.Location|X</code>)
     * 
     * @Since 15.14 
     */
    @Getter @Setter private String queryStringParamName;
    
    /** Value of the query string parameter for this value (e.g. <code>Syndey</code>)
     *  
     * @Since 15.14 
     */
    @Getter @Setter private String queryStringParamValue;
}
