package com.funnelback.publicui.search.model.padre;

import java.util.ArrayList;
import java.util.List;

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
    
    @Getter private final List<GroupAndSum> groupAndSums = new ArrayList<>();
 
    /**
     * <p>Holds the sum of numeric values in a group</p>
     * 
     * @since 15.8
     */
    @AllArgsConstructor
    @EqualsAndHashCode
    @ToString
    public static class GroupAndSum {
        
        /**
         * <p>The group the sum is for.</p>
         * 
         * <p>The group is a single value from the metadata class 
         * we are grouping on.</p>
         * 
         * @since 15.8
         */
        @Getter private final String group;
        
        /**
         * <p>The sum of all numeric metadata values in the group</p>
         * 
         * @since 15.8
         */
        @Getter private final long sum;
    }
}