package com.funnelback.publicui.search.model.transaction.facet.order;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;



public class ByLabelComparatorTest {

    @Test
    public void testSameLabels() {
        Assert.assertEquals(0, 
            new ByLabelComparator().compare(categoryWithLabel("w"), categoryWithLabel("W")));
    }
    
    @Test
    public void testLabelsDiffernt() {
        Assert.assertEquals(-1, 
            new ByLabelComparator().compare(categoryWithLabel("a"), categoryWithLabel("b")));
        
        Assert.assertEquals(1, 
            new ByLabelComparator().compare(categoryWithLabel("c"), categoryWithLabel("b")));
    }
    
    private CategoryValue categoryWithLabel(String label) {
        CategoryValue catVal = mock(CategoryValue.class);
        when(catVal.getLabel()).thenReturn(label);
        return catVal;
    }
}
