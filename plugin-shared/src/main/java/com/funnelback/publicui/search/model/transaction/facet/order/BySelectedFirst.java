package com.funnelback.publicui.search.model.transaction.facet.order;

import java.util.Comparator;

import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;

public class BySelectedFirst implements Comparator<Facet.CategoryValue> {

    @Override
    public int compare(CategoryValue o1, CategoryValue o2) {
        if(o1.isSelected() && o2.isSelected()) {
            // If both are selected we want to ensure selected category values
            // which some from nested category definitions preserve the nested order.
            return Integer.compare(o1.getCategoryValueDepth(), o2.getCategoryValueDepth());
        }
        if(o1.isSelected()) {
            return -1;
        }
        
        if(o2.isSelected()) {
            return 1;
        }
        
        return 0;
    }

}
