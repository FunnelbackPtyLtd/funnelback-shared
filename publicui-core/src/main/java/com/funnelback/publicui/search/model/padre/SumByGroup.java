package com.funnelback.publicui.search.model.padre;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class SumByGroup {
    
    @Getter private final String by;
    @Getter private final String on;
    
    @Getter private final List<GroupAndSum> groupAndSums = new ArrayList<>();
 
    @AllArgsConstructor
    @EqualsAndHashCode
    @ToString
    public static class GroupAndSum {
        @Getter private final String group;
        @Getter private final long sum;
    }
}