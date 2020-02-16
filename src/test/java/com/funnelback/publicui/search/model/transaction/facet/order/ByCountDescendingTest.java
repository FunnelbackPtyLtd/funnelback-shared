package com.funnelback.publicui.search.model.transaction.facet.order;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;

import org.junit.runners.Parameterized.Parameters;

import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;

public class ByCountDescendingTest extends FacetComparatorBaseTest {
    
    @Override
    public Comparator<CategoryValue> getComparator() {
        return FacetComparators.BY_COUNT_DESCENDING;
    }
    
    @Override
    public CategoryValue getNonNullValue() {
        return forCount(0);
    }
    
    @Override
    public Optional<CategoryValue> getNullValue() {
        return Optional.of(forCount(null));
    }
    
    @Override
    public boolean getNullsLast() {
        return true;
    }
    
    @Parameters
    public static Collection<Object[]> data() {
        CategoryValue cv_10 = forCount(10);
        CategoryValue cv_1 = forCount(1);
        CategoryValue cv_0 = forCount(0);
        CategoryValue cv_9 = forCount(9);
        
        return Arrays.asList(new Object[][] {     
             { Arrays.asList(cv_10, cv_1, cv_0, cv_9), 
                 Arrays.asList(cv_10, cv_9, cv_1, cv_0)},  
            { Arrays.asList(cv_0), 
                     Arrays.asList(cv_0)},
           });
    }
    
    private static CategoryValue forCount(Integer count) {
        CategoryValue v = mock(CategoryValue.class);
        when(v.getCount()).thenReturn(count);
        return v;
    }

    

    
}
