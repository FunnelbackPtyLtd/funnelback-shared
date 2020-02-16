package com.funnelback.publicui.search.model.transaction.facet.order;

import java.util.Comparator;
import java.util.Optional;

import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.Facet.Category;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ByCountOfFirstCategoryValueComparator implements Comparator<Facet.Category> {
    
    @Override
    public int compare(Facet.Category c1, Facet.Category c2) {
        if (c1.getValues().size() > 0 && c2.getValues().size() > 0) {
            return Optional.ofNullable(c2.getValues().get(0).getCount()).orElse(Integer.MIN_VALUE) 
                    - Optional.ofNullable(c1.getValues().get(0).getCount()).orElse(Integer.MIN_VALUE);
        } else if (c1.getValues().size() > 0) {
            return 1;
        } else {
            return 2;
        }
    }
}