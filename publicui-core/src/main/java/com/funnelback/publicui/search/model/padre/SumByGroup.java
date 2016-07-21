package com.funnelback.publicui.search.model.padre;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

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
     * <p>The metadata class we are grouping on.</p>
     * 
     * @since 15.8
     */
    @Getter private final String by;
    
    /**
     * <p>The numeric metadata class we are summing on.</p>
     * @since 15.8 
     */
    @Getter private final String on;
    
    /**
     * <p>Holds the sum of numeric values in a group</p>
     * 
     * <p>The key is the group, which is a single value from the metadata class <code>by</code>.
     * The value is the sum of all numeric metadata values in the group  for metadata
     * class <code>on</code>.</p>
     * @since 15.8
     */
    @Getter private final Map<String, Long> groupAndSums = new HashMap<>();
 
}