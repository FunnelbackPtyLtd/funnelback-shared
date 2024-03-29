package com.funnelback.publicui.search.model.transaction.facet.order;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.*;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;

public class ByLabelAsNumberAscendingTest extends FacetComparatorBaseTest {
    
    @Override
    public Comparator<CategoryValue> getComparator() {
        return FacetComparators.BY_LABEL_AS_NUMBER_ASCENDING;
    }
    
    @Override
    public CategoryValue getNonNullValue() {
        return forLabel("0");
    }
    
    @Override
    public Optional<CategoryValue> getNullValue() {
        return Optional.of(forLabel(null));
    }
    
    @Override
    public boolean getNullsLast() {
        return true;
    }

    protected static Stream<Arguments> data() {
        CategoryValue cv_10 = forLabel("10");
        CategoryValue cv_1 = forLabel("1");
        CategoryValue cv_0 = forLabel("0");
        CategoryValue cv_9 = forLabel("9");
        CategoryValue cv_null = forLabel(null);

        return Stream.of(
            Arguments.of(Arrays.asList(cv_10, cv_1, cv_null, cv_0, cv_9), Arrays.asList(cv_0, cv_1, cv_9, cv_10, cv_null)),
            Arguments.of(List.of(cv_0),  List.of(cv_0))
        );
    }
    
    private static CategoryValue forLabel(String label) {
        CategoryValue catVal = mock(CategoryValue.class);
        when(catVal.getLabel()).thenReturn(label);
        when(catVal.toString()).thenReturn("Label: " + label);
        return catVal;
    }
}