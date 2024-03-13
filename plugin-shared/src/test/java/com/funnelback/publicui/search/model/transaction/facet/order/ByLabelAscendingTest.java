package com.funnelback.publicui.search.model.transaction.facet.order;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;

public class ByLabelAscendingTest extends FacetComparatorBaseTest {
    
    @Override
    public Comparator<CategoryValue> getComparator() {
        return FacetComparators.BY_LABEL_ASCENDING;
    }
    
    @Override
    public CategoryValue getNonNullValue() {
        return forLabel("a");
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
        CategoryValue cv_a = forLabel("a");
        CategoryValue cv_A = forLabel("A");
        CategoryValue cv_b = forLabel("b");
        CategoryValue cv_c = forLabel("c");
        CategoryValue cv_d = forLabel("d");

        return Stream.of(
            Arguments.of(Arrays.asList(cv_b, cv_c, cv_a, cv_A, cv_d), Arrays.asList(cv_a, cv_A, cv_b, cv_c, cv_d))
        );
    }
    
    private static CategoryValue forLabel(String label) {
        CategoryValue catVal = mock(CategoryValue.class);
        when(catVal.getLabel()).thenReturn(label);
        when(catVal.toString()).thenReturn("Label: " + label);
        return catVal;
    }
}