package com.funnelback.publicui.search.model.transaction.facet.order;

import static com.funnelback.publicui.search.model.transaction.facet.order.FacetComparators.BY_COUNT_ASCENDING;
import static com.funnelback.publicui.search.model.transaction.facet.order.FacetComparators.BY_COUNT_DESCENDING;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;


public class ByCountComparatorTest {

    @Test
    public void testWithNullValues() {
        CategoryValue catV = mock(CategoryValue.class);
        when(catV.getCount()).thenReturn(null);
        Assert.assertEquals(0, 
            BY_COUNT_ASCENDING.compare(catV, catV));
    }
    
    
    @Test
    public void testWithNullSingleValue() {
        
        CategoryValue catVNull = mock(CategoryValue.class);
        when(catVNull.getCount()).thenReturn(null);
        
        CategoryValue catCountSet = mock(CategoryValue.class);
        when(catCountSet.getCount()).thenReturn(-2);
        
        List<CategoryValue> catValues = new ArrayList<>();
        catValues.add(catCountSet);
        catValues.add(catVNull);
        
        
        Collections.sort(catValues, BY_COUNT_DESCENDING);
        
        Assert.assertEquals(catCountSet, catValues.get(0));
        Assert.assertEquals(catVNull, catValues.get(1));
    }
    
    @Test
    public void testWithNullSingleValue2() {
        CategoryValue catVNull = mock(CategoryValue.class);
        when(catVNull.getCount()).thenReturn(null);
        
        CategoryValue catCountSet = mock(CategoryValue.class);
        when(catCountSet.getCount()).thenReturn(-2);
        
        List<CategoryValue> catValues = new ArrayList<>();
        catValues.add(catVNull);
        catValues.add(catCountSet);
        
        
        
        Collections.sort(catValues, BY_COUNT_DESCENDING);
        
        Assert.assertEquals(catCountSet, catValues.get(0));
        Assert.assertEquals(catVNull, catValues.get(1));
    }
}
