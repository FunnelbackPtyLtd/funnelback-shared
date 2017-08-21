package com.funnelback.publicui.search.model.transaction.facet.order;

import java.util.Comparator;

import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;

public class BySelectedFirst implements Comparator<Facet.CategoryValue> {

    @Override
    public int compare(CategoryValue o1, CategoryValue o2) {
        if(o1.isSelected() && o2.isSelected()) {
            return Integer.compare(o1.getCategoryDepth(), o2.getCategoryDepth());
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
