package com.funnelback.publicui.search.model.transaction.facet.order;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;


public class BySelectedFirstTest extends FacetComparatorBaseTest {
    
    @Override
    public Comparator<CategoryValue> getComparator() {
        return new BySelectedFirst();
    }

    @Parameters
    public static Collection<Object[]> data() {
        CategoryValue cv_true_0 = categoryWithSelection(true, 0);
        CategoryValue cv_true_5 = categoryWithSelection(true, 5);
        CategoryValue cv_true_6 = categoryWithSelection(true, 6);
        CategoryValue cv_true_10 = categoryWithSelection(true, 10);
        CategoryValue cv_false_1 = categoryWithSelection(false, 1);
        CategoryValue cv_false_2 = categoryWithSelection(false, 2);
        
        return Arrays.asList(new Object[][] {     
             { Arrays.asList(cv_true_10, cv_true_6, cv_false_1, cv_false_2, cv_true_5, cv_true_0), 
                 Arrays.asList(cv_true_0, cv_true_5, cv_true_6, cv_true_10, cv_false_1, cv_false_2)},
           });
    }

    @Override
    public CategoryValue getNonNullValue() {
        return categoryWithSelection(true, 0);
    }


    @Override
    public Optional<CategoryValue> getNullValue() {
        return Optional.empty();
    }
    
    @Override
    public boolean getNullsLast() {
        throw new IllegalArgumentException("Nulls not supported.");
    }
    
    @Test
    public void bothSelected() {
        Assert.assertEquals(0, 
            new BySelectedFirst().compare(categoryWithSelection(true,1), categoryWithSelection(true,1)));
        
        Assert.assertEquals(-1, 
            new BySelectedFirst().compare(categoryWithSelection(true,0), categoryWithSelection(true,1)));
        
        Assert.assertEquals(1, 
            new BySelectedFirst().compare(categoryWithSelection(true,1), categoryWithSelection(true,0)));
    }
    
    
    @Test
    public void bothNotSelected() {
        Assert.assertEquals(0, 
            new BySelectedFirst().compare(categoryWithSelection(false,1), categoryWithSelection(false,1)));
    }
    
    @Test
    public void oneSelected() {
        Assert.assertEquals(-1, 
            new BySelectedFirst().compare(categoryWithSelection(true,0), categoryWithSelection(false,1)));
        
        Assert.assertEquals(-1, 
            new BySelectedFirst().compare(categoryWithSelection(true,1), categoryWithSelection(false,0)));
        
        Assert.assertEquals(1, 
            new BySelectedFirst().compare(categoryWithSelection(false,0), categoryWithSelection(true,1)));
        
        Assert.assertEquals(1, 
            new BySelectedFirst().compare(categoryWithSelection(false,1), categoryWithSelection(true,0)));
    }

    static CategoryValue categoryWithSelection(boolean selected, int depth) {
        CategoryValue catVal = mock(CategoryValue.class);
        when(catVal.isSelected()).thenReturn(selected);
        when(catVal.getCategoryValueDepth()).thenReturn(depth);
        when(catVal.toString()).thenReturn("Selected: " + selected + " depth: " + depth);
        return catVal;
    }

}
