package com.funnelback.publicui.search.model.transaction.facet.order;

import static org.assertj.core.api.Assertions.assertThat;

import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

abstract class FacetComparatorBaseTest {

    protected abstract Comparator<CategoryValue> getComparator();

    protected abstract CategoryValue getNonNullValue();

    protected abstract Optional<CategoryValue> getNullValue();

    protected abstract boolean getNullsLast();

    @ParameterizedTest
    @MethodSource("data")
    void testSorting(List<CategoryValue> input, List<CategoryValue> sorted) {
        List<CategoryValue> toSort = new ArrayList<>(input);
        toSort.sort(getComparator());
        assertThat(toSort).isEqualTo(sorted);
    }

    @ParameterizedTest
    @MethodSource("data")
    void testNulls() {
        Assumptions.assumeTrue(getNullValue().isPresent());
        assertThat(getComparator().compare(getNullValue().get(), getNullValue().get())).isZero();
    }

    @ParameterizedTest
    @MethodSource("data")
    void testNullsOrder() {
        Assumptions.assumeTrue(getNullValue().isPresent());
        int o1 = getComparator().compare(getNullValue().get(), getNonNullValue());
        int o2 = getComparator().compare(getNonNullValue(), getNullValue().get());

        if (getNullsLast()) {
            assertThat(o1).isPositive();
            assertThat(o2).isNegative();
        } else {
            assertThat(o1).isNegative();
            assertThat(o2).isPositive();
        }
    }

    @ParameterizedTest
    @MethodSource("data")
    void testNullVsNonNull() {
        Assumptions.assumeTrue(getNullValue().isPresent());
        int o1 = getComparator().compare(getNullValue().get(), getNonNullValue());
        int o2 = getComparator().compare(getNonNullValue(), getNullValue().get());
        assertThat(o1).isNotZero();
        assertThat(o2).isNotZero();
        assertThat(o1).isEqualTo(o2 * -1);
        assertThat(o1).isNotEqualTo(o2);
    }

    @ParameterizedTest
    @MethodSource("data")
    void testEquals(List<CategoryValue> input) {
        for (CategoryValue cv : input) {
            assertThat(getComparator().compare(cv, cv)).isZero();
        }
    }

    @ParameterizedTest
    @MethodSource("data")
    void testReversible(List<CategoryValue> input) {
        for (int i = 0; i < input.size(); i++) {
            CategoryValue cv1 = input.get(i);
            for (int j = 0; j < input.size(); j++) {
                CategoryValue cv2 = input.get(j);
                int order1 = getComparator().compare(cv1, cv2);
                int order2 = getComparator().compare(cv2, cv1);

                assertThat(order1)
                    .as(
                        """
                            Something is wrong in the way comparison is done! This test took:
                            CategoryValues %d: %s
                            %d: %s
                            Comparing one way gave: %d comparing the other way gave %d multiplying by -1 should give the other!""",
                        i,
                        cv1,
                        j,
                        cv2,
                        order1,
                        order2)
                    .isEqualTo(order2 * -1);
            }
        }
    }

    @ParameterizedTest
    @MethodSource("data")
    void testReversibleAgainstNulls(List<CategoryValue> input) {
        Assumptions.assumeTrue(getNullValue().isPresent());
        for (int i = 0; i < input.size(); i++) {
            CategoryValue cv1 = input.get(i);
            CategoryValue cv2 = getNullValue().get();
            int order1 = getComparator().compare(cv1, cv2);
            int order2 = getComparator().compare(cv2, cv1);

            assertThat(order1)
                .as(
                    """
                        Something is wrong in the way comparison is done! This test took:
                        CategoryValues %d: %s
                         vs a null value
                        Comparing one way gave: %d comparing the other way gave %d multiplying by -1 should give the other!""",
                    i,
                    cv1,
                    order1,
                    order2)
                .isEqualTo(order2 * -1);
        }
    }
}
