package com.funnelback.publicui.search.model.transaction.facet.order;

import static com.funnelback.common.facetednavigation.models.FacetValuesOrder.COUNT_ASCENDING;
import static com.funnelback.common.facetednavigation.models.FacetValuesOrder.COUNT_DESCENDING;
import static com.funnelback.common.facetednavigation.models.FacetValuesOrder.LABEL_ASCENDING;
import static com.funnelback.common.facetednavigation.models.FacetValuesOrder.LABEL_DESCENDING;
import static com.funnelback.common.facetednavigation.models.FacetValuesOrder.SELECTED_FIRST;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.funnelback.common.facetednavigation.models.FacetValuesOrder;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FacetComparatorProviderTest {
    
    // These are made global otherwise we need to override the equals, and I am too lazy to do that.
    // I use snake and _ because I want the tests to be easier to read.

    private final CategoryValue catWithCount_1 = categoryWithCount(1);
    private final CategoryValue catWithCount_5 = categoryWithCount(5);
    private final CategoryValue catWithCount_10 = categoryWithCount(10);
    
    // Note the capital W here.
    private final CategoryValue catWithLabel_Whisky = categoryWithLabel("Whisky");
    private final CategoryValue catWithLabel_test = categoryWithLabel("test");
    private final CategoryValue catWithLabel_apple = categoryWithLabel("apple");
    
    private final CategoryValue catWithSelection_true_depth_1 = categoryWithSelection(true, 1);
    private final CategoryValue catWithSelection_false_depth_1 = categoryWithSelection(false, 1);
    
    private final CategoryValue catWithSelection_true_depth_2 = categoryWithSelection(true, 2);
    private final CategoryValue catWithSelection_false_depth_2 = categoryWithSelection(false, 2);
    
    
    @Test
    public void testCountAscending() {
        test(List.of(COUNT_ASCENDING),
            asList(catWithCount_10, catWithCount_1, catWithCount_5), 
            asList(catWithCount_1,catWithCount_5, catWithCount_10));
    }
    
    @Test
    public void testCountDescending() {
        test(List.of(COUNT_DESCENDING),
            asList(catWithCount_10, catWithCount_1, catWithCount_5), 
            asList(catWithCount_10,catWithCount_5, catWithCount_1));
    }
    
    @Test
    public void testLabelAscending() {
        test(List.of(LABEL_ASCENDING),
            asList(catWithLabel_Whisky, catWithLabel_apple, catWithLabel_test), 
            asList(catWithLabel_apple, catWithLabel_test, catWithLabel_Whisky));
    }
    
    @Test
    public void testLabelDescending() {
        test(List.of(LABEL_DESCENDING),
            asList(catWithLabel_Whisky, catWithLabel_apple, catWithLabel_test), 
            asList(catWithLabel_Whisky, catWithLabel_test, catWithLabel_apple));
    }
    
    @Test
    public void testSelectedFirst() {
        test(List.of(SELECTED_FIRST),
            asList(catWithSelection_false_depth_1, catWithSelection_true_depth_1), 
            asList(catWithSelection_true_depth_1, catWithSelection_false_depth_1));
    }
    
    @Test
    public void testSelectedFirstDepthOfCategory() {
        test(List.of(SELECTED_FIRST),
            asList(catWithSelection_true_depth_2, catWithSelection_true_depth_1), 
            asList(catWithSelection_true_depth_1, catWithSelection_true_depth_2));
    }
    
    @Test
    public void testNoValues() {
        test(List.of(),
            asList(catWithSelection_false_depth_1, catWithSelection_true_depth_1), 
            asList(catWithSelection_false_depth_1, catWithSelection_true_depth_1));
    }
    
    @Test
    public void testAllSortTypeHaveAComparator() {
        for(FacetValuesOrder order : FacetValuesOrder.values()) {
            Assertions.assertNotNull(new FacetComparatorProvider().getComparator(order, Optional.empty()), "Missing comparator for : " + order);
        }
    }
    

    public void test(List<FacetValuesOrder> order,
        List<CategoryValue> toSort,
        List<CategoryValue> expectedOrder) {
        List<CategoryValue> ordered = new ArrayList<>(toSort);
        
        ordered.sort(new FacetComparatorProvider().getComparatorWhenSortingAllValues(order, Optional.empty()));
        
        Assertions.assertEquals(toSort.size(), expectedOrder.size());
        
        for(int i = 0; i < toSort.size(); i++) {
            Assertions.assertEquals(expectedOrder.get(i), ordered.get(i));
        }
    }
    
    private CategoryValue categoryWithCount(int count) {
        CategoryValue catVal = mock(CategoryValue.class);
        when(catVal.getCount()).thenReturn(count);
        return catVal;
    }
    
    private CategoryValue categoryWithLabel(String label) {
        CategoryValue catVal = mock(CategoryValue.class);
        when(catVal.getLabel()).thenReturn(label);
        return catVal;
    }
    
    private CategoryValue categoryWithSelection(boolean selected, int depth) {
        CategoryValue catVal = mock(CategoryValue.class);
        when(catVal.isSelected()).thenReturn(selected);
        when(catVal.getCategoryValueDepth()).thenReturn(depth);
        return catVal;
    }
}