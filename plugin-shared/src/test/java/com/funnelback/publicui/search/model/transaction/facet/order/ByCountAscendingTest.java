package com.funnelback.publicui.search.model.transaction.facet.order;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.*;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;

public class ByCountAscendingTest extends FacetComparatorBaseTest {

    @Override
    public Comparator<CategoryValue> getComparator() {
        return FacetComparators.BY_COUNT_ASCENDING;
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

    protected static Stream<Arguments> data() {
        CategoryValue cv_10 = forCount(10);
        CategoryValue cv_1 = forCount(1);
        CategoryValue cv_0 = forCount(0);
        CategoryValue cv_9 = forCount(9);

        return Stream.of(
            Arguments.of(Arrays.asList(cv_10, cv_1, cv_0, cv_9), Arrays.asList(cv_0, cv_1, cv_9, cv_10)),
            Arguments.of(List.of(cv_0), List.of(cv_0))
        );
    }

    private static CategoryValue forCount(Integer count) {
        CategoryValue v = mock(CategoryValue.class);
        when(v.getCount()).thenReturn(count);
        return v;
    }
}
