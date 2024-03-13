package com.funnelback.publicui.search.model.transaction.facet.order;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.funnelback.common.facetednavigation.models.FacetValuesOrder;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;

public class RNDSUPPORT3067Test {

    private final FacetComparatorProvider comparatorProvider = new FacetComparatorProvider();
    
    @Test
    public void test() throws Exception {
        List<FacetValuesOrder> order = Arrays.asList(FacetValuesOrder.SELECTED_FIRST, FacetValuesOrder.COUNT_DESCENDING);
        
        List<CategoryValue> values = new ArrayList<>();
        values.add(new CategoryValue("a", "a", null, true, "", "", "", 0, 0));
        values.add(new CategoryValue("a", "a", null, false, "", "", "", 0, 0));
        values.add(new CategoryValue("a", "a", null,false, "", "", "", 0, 0));
        values.add(new CategoryValue("a", "a", null,false, "", "", "", 0, 0));
        values.add(new CategoryValue("a", "a", null,false, "", "", "", 0, 0));
        values.add(new CategoryValue("a", "a", null,false, "", "", "", 0, 0));
        values.add(new CategoryValue("a", "a", null,false, "", "", "", 0, 0));
        values.add(new CategoryValue("a", "a", null,false, "", "", "", 0, 0));
        values.add(new CategoryValue("a", "a", null,true, "", "", "", 0, 0));
        values.add(new CategoryValue("a", "a", null,false, "", "", "", 0, 0));
        values.add(new CategoryValue("a", "a", null,false, "", "", "", 0, 0));
        values.add(new CategoryValue("a", "a", null,false, "", "", "", 0, 0));
        values.add(new CategoryValue("a", "a", null,false, "", "", "", 0, 0));
        values.add(new CategoryValue("a", "a", null,false, "", "", "", 0, 0));
        values.add(new CategoryValue("a", "a", null,false, "", "", "", 0, 0));
        values.add(new CategoryValue("a", "a", null,false, "", "", "", 0, 0));
        values.add(new CategoryValue("a", "a", null,false, "", "", "", 0, 0));
        values.add(new CategoryValue("a", "a", null,false, "", "", "", 0, 0));
        values.add(new CategoryValue("a", "a", null,false, "", "", "", 0, 0));
        values.add(new CategoryValue("a", "a", null,false, "", "", "", 0, 0));
        values.add(new CategoryValue("a", "a", null,true, "", "", "", 0, 0));
        values.add(new CategoryValue("a", "a", null,false, "", "", "", 0, 0));
        values.add(new CategoryValue("a", "a", null,false, "", "", "", 0, 0));
        values.add(new CategoryValue("a", "a", null,false, "", "", "", 0, 0));
        values.add(new CategoryValue("a", "a", null,false, "", "", "", 0, 0));
        values.add(new CategoryValue("a", "a", null,true, "", "", "", 0, 0));
        values.add(new CategoryValue("a", "a", null,true, "", "", "", 0, 0));
        values.add(new CategoryValue("a", "a", null,false, "", "", "", 0, 0));
        values.add(new CategoryValue("a", "a", null,true, "", "", "", 0, 0));
        values.add(new CategoryValue("a", "a", null,false, "", "", "", 0, 0));
        values.add(new CategoryValue("a", "a", null,true, "", "", "", 0, 0));
        values.add(new CategoryValue("a", "a", null,false, "", "", "", 0, 0));
        values.add(new CategoryValue("a", "a", null,false, "", "", "", 0, 0));
        values.add(new CategoryValue("a", "a", 0, true, "", "", "", 0, 0));
        
        values.sort(comparatorProvider.getComparatorWhenSortingAllValues(order, Optional.empty()));
    }
}