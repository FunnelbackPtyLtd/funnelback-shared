package com.funnelback.publicui.search.model.transaction.facet.order;

import java.util.Comparator;

import com.funnelback.publicui.search.model.transaction.Facet;

import lombok.RequiredArgsConstructor;

/**
 * This is used in the old sorting model where for gscopes we had to sort the categories
 * and hoped that each category had exactly one value.
 *
 */
@RequiredArgsConstructor
public class ByFirstCategoryValueComparator implements Comparator<Facet.Category> {
    
    private final Comparator<Facet.CategoryValue> valueComparator;
    
    @Override
    public int compare(Facet.Category c1, Facet.Category c2) {
        if (c1.getValues().size() > 0 && c2.getValues().size() > 0) {
            return valueComparator.compare(c1.getValues().get(0), c2.getValues().get(0));
        } else if (c1.getValues().size() > 0) {
            return 1;
        } else {
            return 2;
        }
    }
}