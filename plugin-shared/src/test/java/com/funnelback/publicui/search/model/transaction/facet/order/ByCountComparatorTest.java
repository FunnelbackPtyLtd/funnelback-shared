package com.funnelback.publicui.search.model.transaction.facet.order;

import static com.funnelback.publicui.search.model.transaction.facet.order.FacetComparators.BY_COUNT_ASCENDING;
import static com.funnelback.publicui.search.model.transaction.facet.order.FacetComparators.BY_COUNT_DESCENDING;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ByCountComparatorTest {

    @Test
    public void testWithNullValues() {
        CategoryValue catV = mock(CategoryValue.class);
        when(catV.getCount()).thenReturn(null);
        Assertions.assertEquals(0, BY_COUNT_ASCENDING.compare(catV, catV));
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
        
        catValues.sort(BY_COUNT_DESCENDING);
        
        Assertions.assertEquals(catCountSet, catValues.get(0));
        Assertions.assertEquals(catVNull, catValues.get(1));
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

        catValues.sort(BY_COUNT_DESCENDING);
        
        Assertions.assertEquals(catCountSet, catValues.get(0));
        Assertions.assertEquals(catVNull, catValues.get(1));
    }
}
