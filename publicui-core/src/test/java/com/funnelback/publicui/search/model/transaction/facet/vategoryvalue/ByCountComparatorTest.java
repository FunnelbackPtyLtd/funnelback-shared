package com.funnelback.publicui.search.model.transaction.facet.vategoryvalue;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue.ByCountComparator;



public class ByCountComparatorTest {

    @Test
    public void testWithNullValues() {
        ByCountComparator comparator = new Facet.CategoryValue.ByCountComparator(true);
        CategoryValue catV = mock(CategoryValue.class);
        when(catV.getCount()).thenReturn(null);
        Assert.assertEquals(0, 
            comparator.compare(catV, catV));
    }
    
    
    @Test
    public void testWithNullSingleValue() {
        ByCountComparator comparator = new Facet.CategoryValue.ByCountComparator(true);
        CategoryValue catVNull = mock(CategoryValue.class);
        when(catVNull.getCount()).thenReturn(null);
        
        CategoryValue catCountSet = mock(CategoryValue.class);
        when(catCountSet.getCount()).thenReturn(-2);
        
        List<CategoryValue> catValues = new ArrayList<>();
        catValues.add(catCountSet);
        catValues.add(catVNull);
        
        
        Collections.sort(catValues, comparator);
        
        Assert.assertEquals(catCountSet, catValues.get(0));
        Assert.assertEquals(catVNull, catValues.get(1));
    }
    
    @Test
    public void testWithNullSingleValue2() {
        ByCountComparator comparator = new Facet.CategoryValue.ByCountComparator(true);
        CategoryValue catVNull = mock(CategoryValue.class);
        when(catVNull.getCount()).thenReturn(null);
        
        CategoryValue catCountSet = mock(CategoryValue.class);
        when(catCountSet.getCount()).thenReturn(-2);
        
        List<CategoryValue> catValues = new ArrayList<>();
        catValues.add(catVNull);
        catValues.add(catCountSet);
        
        
        
        Collections.sort(catValues, comparator);
        
        Assert.assertEquals(catCountSet, catValues.get(0));
        Assert.assertEquals(catVNull, catValues.get(1));
    }
}
