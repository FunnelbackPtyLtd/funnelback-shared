package com.funnelback.publicui.search.model.transaction.facet.order;

import java.util.Comparator;
import java.util.Optional;

import com.funnelback.publicui.search.model.transaction.Facet;

import lombok.RequiredArgsConstructor;

/**
 * Compares category values by number of occurrences
 */
@RequiredArgsConstructor
public class ByCountAscending implements Comparator<Facet.CategoryValue> {
    
    @Override
    public int compare(Facet.CategoryValue c1, Facet.CategoryValue c2) {
        
        return (Optional.ofNullable(c1.getCount()).orElse(Integer.MIN_VALUE) - 
                Optional.ofNullable(c2.getCount()).orElse(Integer.MIN_VALUE));
    }

}