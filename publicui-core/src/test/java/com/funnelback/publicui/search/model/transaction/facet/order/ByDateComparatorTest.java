package com.funnelback.publicui.search.model.transaction.facet.order;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.common.config.DefaultValues.FacetedNavigation.DateSortMode;
import com.funnelback.common.function.StreamUtils;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;

public class ByDateComparatorTest {

    @Test
    public void testWithUnknownValue() {
        List<CategoryValue> cats = getCats("Past week", "Today", "Yesterday", "2000", "1996", "lol what?");
        
        cats.sort(new ByDateComparator(DateSortMode.ddate));
        
        Assert.assertEquals(Arrays.asList("Today","Yesterday","Past week","2000","1996","lol what?"), 
            cats.stream().map(CategoryValue::getLabel).collect(Collectors.toList()));
        
        cats.sort(new ByDateComparator(DateSortMode.adate));
        
        Assert.assertEquals(Arrays.asList("1996","2000","Past week","Yesterday","Today","lol what?"), 
            cats.stream().map(CategoryValue::getLabel).collect(Collectors.toList()));
    }
    
    @Test
    public void testNull() {
        new ByDateComparator(DateSortMode.adate).compare(getCat(""), getCat(""));
    }
    
    public List<CategoryValue> getCats(String ... labels) {
        return StreamUtils.ofNullable(labels)
            .map(this::getCat)
            .collect(Collectors.toList());
    }
    
    public CategoryValue getCat(String label) {
        CategoryValue categoryValue = mock(CategoryValue.class);
        when(categoryValue.getLabel()).thenReturn(label);
        return categoryValue;
    }
}
