package com.funnelback.publicui.search.model.transaction;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.common.facetednavigation.models.FacetConstraintJoin;
import com.funnelback.common.facetednavigation.models.FacetSelectionType;
import com.funnelback.common.facetednavigation.models.FacetValues;
import com.funnelback.common.facetednavigation.models.FacetValuesOrder;
import com.funnelback.publicui.search.model.transaction.Facet.Category;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.facet.order.ByLabelComparator;

public class FacetTest {

    @Test
    public void testCustomSorting() {
        Facet facet = new Facet("", FacetSelectionType.SINGLE,
                    FacetConstraintJoin.AND,
                    FacetValues.FROM_SCOPED_QUERY,
                    new ArrayList<>(Arrays.asList(FacetValuesOrder.LABEL_ASCENDING, FacetValuesOrder.COUNT_DESCENDING)));
        
        facet.getCategories().add(new Category("", ""));
        facet.getCategories().get(0).getValues().addAll(Arrays.asList(
            categoryWithLabel("b", "b", 10),
            categoryWithLabel("a", "a2", 2 ),
            categoryWithLabel("a", "a1", 1 ),
            categoryWithLabel("a", "a3", 3 ),
            categoryWithLabel("c", "c", 4)
            ));
        
        expectOrderOfValues("Should follow label order", facet, "a3", "a2", "a1", "b", "c");
        
        facet.setCustomComparator(new ByLabelComparator().reversed());
        
        expectOrderOfValues("A custom comparator is added but we the order does not say to use it.",
            facet, "a3", "a2", "a1", "b", "c");
        
        facet.getOrder().removeIf(e -> true);
        
        facet.getOrder().addAll(Arrays.asList(FacetValuesOrder.CUSTOM_COMPARATOR, FacetValuesOrder.COUNT_DESCENDING));
        
        expectOrderOfValues("The custom comparator should have been enabled.",
            facet, "c", "b", "a3", "a2", "a1");
        
        facet.setCustomComparator(null);
        
        expectOrderOfValues("The custom comparator is enabled but it is not set, should "
            + "use the sort order of the built in comparator only.",
            facet, "b", "c", "a3", "a2", "a1");
        
    }
    
    public void expectOrderOfValues(String msg, Facet facet, String ... values) {
        List<String> actualValues = facet.getAllValues()
            .stream()
            .map(CategoryValue::getData)
            .collect(Collectors.toList());
        for(int i = 0; i < values.length; i++) {
            String expected = values[i];
            String actualValue = actualValues.get(i);
            Assert.assertEquals(msg + " @ " + i, expected, actualValue);
        }
    }
    
    
    private CategoryValue categoryWithLabel(String label, String data, int count) {
        CategoryValue catVal = mock(CategoryValue.class);
        when(catVal.getLabel()).thenReturn(label);
        when(catVal.getCount()).thenReturn(count);
        when(catVal.getData()).thenReturn(data);
        return catVal;
    }
}
