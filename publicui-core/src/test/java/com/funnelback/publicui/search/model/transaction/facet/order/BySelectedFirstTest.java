package com.funnelback.publicui.search.model.transaction.facet.order;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;


public class BySelectedFirstTest {
    
    @Test
    public void bothSelected() {
        Assert.assertEquals(0, 
            new BySelectedFirst().compare(categoryWithSelection(true), categoryWithSelection(true)));
    }
    
    
    @Test
    public void bothNotSelected() {
        Assert.assertEquals(0, 
            new BySelectedFirst().compare(categoryWithSelection(false), categoryWithSelection(false)));
    }
    
    @Test
    public void oneSelected() {
        Assert.assertEquals(-1, 
            new BySelectedFirst().compare(categoryWithSelection(true), categoryWithSelection(false)));
        Assert.assertEquals(1, 
            new BySelectedFirst().compare(categoryWithSelection(false), categoryWithSelection(true)));
    }

    CategoryValue categoryWithSelection(boolean selected) {
        CategoryValue catVal = mock(CategoryValue.class);
        when(catVal.isSelected()).thenReturn(selected);
        return catVal;
    }
}
