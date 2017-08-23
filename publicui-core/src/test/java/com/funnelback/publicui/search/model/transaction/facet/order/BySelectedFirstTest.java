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

    CategoryValue categoryWithSelection(boolean selected, int depth) {
        CategoryValue catVal = mock(CategoryValue.class);
        when(catVal.isSelected()).thenReturn(selected);
        when(catVal.getCategoryDepth()).thenReturn(depth);
        return catVal;
    }
}
