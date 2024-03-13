package com.funnelback.publicui.search.model.transaction.facet.order;

import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public abstract class FacetComparatorBaseTest {

    protected abstract Comparator<CategoryValue> getComparator();
    
    protected abstract CategoryValue getNonNullValue();
    
    protected abstract Optional<CategoryValue> getNullValue();
    
    protected abstract boolean getNullsLast();

    @ParameterizedTest
    @MethodSource("data")
    public void testSorting(List<CategoryValue> input, List<CategoryValue> sorted) {
        List<CategoryValue> toSort = new ArrayList<>(input);
        toSort.sort(getComparator());
        Assertions.assertEquals(sorted, toSort);
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testNulls() {
        Assumptions.assumeTrue(getNullValue().isPresent());
        Assertions.assertEquals(0, getComparator().compare(getNullValue().get(), getNullValue().get()));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testNullsOrder() {
        Assumptions.assumeTrue(getNullValue().isPresent());
        int o1 = getComparator().compare(getNullValue().get(), getNonNullValue());
        int o2 = getComparator().compare(getNonNullValue(), getNullValue().get());
        
        if(getNullsLast()) {
            Assertions.assertTrue(o1 > 0);
            Assertions.assertTrue(o2 < 0);
        } else {
            Assertions.assertTrue(o1 < 0);
            Assertions.assertTrue(o2 > 0);
        }
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testNullVsNonNull() {
        Assumptions.assumeTrue(getNullValue().isPresent());
        int o1 = getComparator().compare(getNullValue().get(), getNonNullValue());
        int o2 = getComparator().compare(getNonNullValue(), getNullValue().get());
        Assertions.assertNotEquals(0, o1);
        Assertions.assertNotEquals(0, o2);
        Assertions.assertEquals(o1, o2*-1);
        Assertions.assertNotEquals(o1, o2);
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testEquals(List<CategoryValue> input) {
        for (CategoryValue cv : input) {
            Assertions.assertEquals(0, getComparator().compare(cv, cv));
        }
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testReversible(List<CategoryValue> input) {
        for (int i = 0; i < input.size(); i++) {
            CategoryValue cv1 = input.get(i);
            for (int j = 0; j < input.size(); j++) {
                CategoryValue cv2 = input.get(j);
                int order1 = getComparator().compare(cv1, cv2);
                int order2 = getComparator().compare(cv2, cv1);

                Assertions.assertEquals(order1, order2 * -1, "Something is wrong in the way comparison is done! This test took:\n"
                    + "CategoryValues " +
                    i + ": " + cv1 + "\n" +
                    j + ": " + cv2 + "\n" +
                    "Comparing one way gave: " + order1 + " comparing the other way gave " + order2 + " multiplying by -1 should give the other!");
            }
        }
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testReversibleAgainstNulls(List<CategoryValue> input) {
        Assumptions.assumeTrue(getNullValue().isPresent());
        for (int i = 0; i < input.size(); i++) {
            CategoryValue cv1 = input.get(i);
            CategoryValue cv2 = getNullValue().get();
            int order1 = getComparator().compare(cv1, cv2);
            int order2 = getComparator().compare(cv2, cv1);

            Assertions.assertEquals(order1, order2 * -1, "Something is wrong in the way comparison is done! This test took:\n"
                + "CategoryValues " +
                i + ": " + cv1 + "\n" +
                " vs a null value\n" +
                "Comparing one way gave: " + order1 + " comparing the other way gave " + order2 + " multiplying by -1 should give the other!");
        }
    }
}