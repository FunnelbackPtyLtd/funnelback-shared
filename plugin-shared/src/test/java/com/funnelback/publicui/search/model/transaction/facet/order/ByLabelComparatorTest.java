package com.funnelback.publicui.search.model.transaction.facet.order;

import static com.funnelback.publicui.search.model.transaction.facet.order.FacetComparators.BY_LABEL_ASCENDING;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ByLabelComparatorTest {

    @Test
    public void testSameLabels() {
        Assertions.assertEquals(0, BY_LABEL_ASCENDING.compare(categoryWithLabel("w"), categoryWithLabel("W")));
    }
    
    @Test
    public void testLabelsDiffer() {
        Assertions.assertEquals(-1, BY_LABEL_ASCENDING.compare(categoryWithLabel("a"), categoryWithLabel("b")));
        
        Assertions.assertEquals(1, BY_LABEL_ASCENDING.compare(categoryWithLabel("c"), categoryWithLabel("b")));
    }
    
    private CategoryValue categoryWithLabel(String label) {
        CategoryValue catVal = mock(CategoryValue.class);
        when(catVal.getLabel()).thenReturn(label);
        return catVal;
    }
}
