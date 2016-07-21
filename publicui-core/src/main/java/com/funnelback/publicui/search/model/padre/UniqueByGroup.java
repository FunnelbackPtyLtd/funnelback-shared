package com.funnelback.publicui.search.model.padre;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * <p>Holds the count of unique values for a metadata class 'on' grouped by the values in metadata class 'by'.</p>
 * 
 * @since 15.8
 */
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class UniqueByGroup {
    
    /**
     * <p>The metadata class we are grouping on.</p>
     * 
     * @since 15.8
     */
    @Getter private final String by;
    
    /**
     * <p>The metadata class we are counting unique values for.</p>
     */
    @Getter private final String on;
    
    /**
     * <p>Holds the count of distinct values in a group.</p> 
     * 
     * <p>The key is the group, which a single value from the metadata class
     * <code>by</code>. The value is the count of unique values in the group
     * for the metadata class <code>on</code>.
     * @since 15.8
     */
    @Getter private final Map<String, Long> groupAndCounts = new HashMap<>();
 
    
    @AllArgsConstructor
    @EqualsAndHashCode
    @ToString
    public static class GroupAndCount {
        
        /**
         * <p>The group the count is for.</p>
         * 
         * <p>The group is a single value from the metadata class 
         * we are grouping on.</p>
         * 
         * @since 15.8
         */
        @Getter private final String group;
        
        /**
         * <p>The count of unique values in the group.</p>
         * 
         * @since 15.8
         */
        @Getter private final long count;
    }
}