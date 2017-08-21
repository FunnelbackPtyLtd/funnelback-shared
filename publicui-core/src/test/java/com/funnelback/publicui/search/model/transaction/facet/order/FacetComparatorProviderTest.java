package com.funnelback.publicui.search.model.transaction.facet.order;

import static com.funnelback.common.facetednavigation.models.FacetValuesOrder.*;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.common.facetednavigation.models.FacetValuesOrder;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
public class FacetComparatorProviderTest {
    
    // These are made global otherwise we need to overridde the equals and I am too 
    // lazy to do that.
    // I use snake and _ because I want the tests to be easier to read.
    
    private final CategoryValue catWithCount_1 = categoryWithCount(1);
    private final CategoryValue catWithCount_5 = categoryWithCount(5);
    private final CategoryValue catWithCount_10 = categoryWithCount(10);
    
    // Note the capital W here.
    private final CategoryValue catWithLabel_Whisky = categoryWithLabel("Whisky");
    private final CategoryValue catWithLabel_test = categoryWithLabel("test");
    private final CategoryValue catWithLabel_apple = categoryWithLabel("apple");
    
    private final CategoryValue catWithSelection_true = categoryWithSelection(true);
    private final CategoryValue catWithSelection_false = categoryWithSelection(false);
    
    
    @Test
    public void testCountAscending() {
        test(asList(COUNT_ASCENDING), 
            asList(catWithCount_10, catWithCount_1, catWithCount_5), 
            asList(catWithCount_1,catWithCount_5, catWithCount_10));
    }
    
    @Test
    public void testCountDescending() {
        test(asList(COUNT_DESCENDING), 
            asList(catWithCount_10, catWithCount_1, catWithCount_5), 
            asList(catWithCount_10,catWithCount_5, catWithCount_1));
    }
    
    @Test
    public void testLabelAscending() {
        test(asList(LABEL_ASCENDING), 
            asList(catWithLabel_Whisky, catWithLabel_apple, catWithLabel_test), 
            asList(catWithLabel_apple, catWithLabel_test, catWithLabel_Whisky));
    }
    
    @Test
    public void testLabelDescending() {
        test(asList(LABEL_DESCENDING), 
            asList(catWithLabel_Whisky, catWithLabel_apple, catWithLabel_test), 
            asList(catWithLabel_Whisky, catWithLabel_test, catWithLabel_apple));
    }
    
    @Test
    public void testSelectedFirst() {
        test(asList(SELECTED_FIRST), 
            asList(catWithSelection_false, catWithSelection_true), 
            asList(catWithSelection_true, catWithSelection_false));
    }
    
    @Test
    public void testNoValues() {
        test(asList(), 
            asList(catWithSelection_false, catWithSelection_true), 
            asList(catWithSelection_false, catWithSelection_true));
    }
    
    @Test
    public void testAllSortTypeHaveAComparator() {
        for(FacetValuesOrder order : FacetValuesOrder.values()) {
            Assert.assertNotNull("Missing comparator for : " + order, 
                new FacetComparatorProvider().getComparator(order));
        }
    }
    

    public void test(List<FacetValuesOrder> order,
        List<CategoryValue> toSort,
        List<CategoryValue> expectedOrder) {
        List<CategoryValue> ordered = new ArrayList<>(toSort);
        
        Collections.sort(ordered, new FacetComparatorProvider().getComparatorWhenSortingValuesFromSingleCategory(order));
        
        Assert.assertEquals(toSort.size(), expectedOrder.size());
        
        for(int i = 0; i < toSort.size(); i++) {
            Assert.assertEquals(expectedOrder.get(i), ordered.get(i));
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
    
    private CategoryValue categoryWithSelection(boolean selected) {
        CategoryValue catVal = mock(CategoryValue.class);
        when(catVal.isSelected()).thenReturn(selected);
        return catVal;
    }
}
