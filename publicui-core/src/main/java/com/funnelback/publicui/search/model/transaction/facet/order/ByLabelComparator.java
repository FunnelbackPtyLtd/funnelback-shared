package com.funnelback.publicui.search.model.transaction.facet.order;

import java.util.Comparator;

import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;

public class ByLabelComparator implements Comparator<CategoryValue> {

    @Override
    public int compare(CategoryValue o1, CategoryValue o2) {
        return o1.getLabel().toLowerCase().compareTo(o2.getLabel().toLowerCase());
    }

}
