package com.funnelback.publicui.search.model.padre;

import java.util.ArrayList;
import java.util.List;

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
    
    @Getter private final List<GroupAndCount> groupAndCounts = new ArrayList<>();
 
    /**
     * <p>Holds the count of distinct values in a group.</p> 
     * 
     * @since 15.8
     */
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