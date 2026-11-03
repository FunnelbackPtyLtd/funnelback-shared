package com.funnelback.publicui.search.model.padre;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Holds the sum of numeric metadata class 'on' grouped by the values in metadata class 'by'.</p>
 * 
 * @since 15.8
 */
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class SumByGroup {
    
    /**
     * <p>The metadata class sums are grouped by.</p>
     * 
     * @since 15.8
     */
    @Getter private final String by;
    
    /**
     * <p>The numeric metadata class being summed.</p>
     * 
     * @since 15.8 
     */
    @Getter private final String on;
    
    /**
     * <p>Holds the sum of numeric values in a group</p>
     * 
     * <p>The key is the group, which is a single value from the metadata class <code>by</code>.
     * The value is the sum of all numeric metadata values in the group for metadata
     * class <code>on</code>.</p>
     * 
     * @since 15.8
     */
    @Getter private final Map<String, Double> groupAndSums = new HashMap<>();
 
}