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
public class UniqueByGroup {
    
    @Getter private final String by;
    @Getter private final String on;
    
    @Getter private final List<GroupAndCount> groupAndCounts = new ArrayList<>();
 
    @AllArgsConstructor
    @EqualsAndHashCode
    @ToString
    public static class GroupAndCount {
        @Getter private final String group;
        @Getter private final long count;
    }
}