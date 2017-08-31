package com.funnelback.publicui.search.model.transaction;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
                    Arrays.asList(FacetValuesOrder.LABEL_ASCENDING));
        
        facet.getCategories().add(new Category("", ""));
        facet.getCategories().get(0).getValues().addAll(Arrays.asList(
            categoryWithLabel("b"),
            categoryWithLabel("a"),
            categoryWithLabel("c")
            ));
        
        List<String> defaultSort = facet.getAllValues()
                                        .stream()
                                        .map(CategoryValue::getLabel)
                                        .collect(Collectors.toList());
        
        Assert.assertEquals("a", defaultSort.get(0));
        Assert.assertEquals("b", defaultSort.get(1));
        Assert.assertEquals("c", defaultSort.get(2));
        
        facet.setValueComparator(new ByLabelComparator().reversed());
        
        List<String> customSort = facet.getAllValues()
            .stream()
            .map(CategoryValue::getLabel)
            .collect(Collectors.toList());

        Assert.assertEquals("c", customSort.get(0));
        Assert.assertEquals("b", customSort.get(1));
        Assert.assertEquals("a", customSort.get(2));
            
    }
    
    
    private CategoryValue categoryWithLabel(String label) {
        CategoryValue catVal = mock(CategoryValue.class);
        when(catVal.getLabel()).thenReturn(label);
        return catVal;
    }
}
