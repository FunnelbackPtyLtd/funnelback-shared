package com.funnelback.publicui.search.model.collection.facetednavigation.impl;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.publicui.search.model.collection.QueryProcessorOption;
import com.google.common.collect.Sets;

public class DateFieldFillTest {

    private DateFieldFill category;
    
    @Before
    public void before() {
        category = new DateFieldFill("d");
        category.setFacetName("facetName");
        category.setLabel("label");
    }
    
    @Test
    public void testFields() {
        Assert.assertEquals("d", category.getData());
        Assert.assertEquals("d", category.getMetadataClass());
        Assert.assertEquals("facetName", category.getFacetName());
        Assert.assertEquals("label", category.getLabel());
        
        Assert.assertEquals(Sets.newHashSet("f.facetName|d"), category.getAllQueryStringParamNames());
    }
    
    @Test
    public void testConstraint() {
        Assert.assertEquals("a value", category.getQueryConstraint("a value"));
    }
    
    @Test
    public void testQPOs() {
        List<QueryProcessorOption<?>> actual = category.getQueryProcessorOptions(null);
        Assert.assertEquals(Collections.singletonList(new QueryProcessorOption<>("count_dates", "d")), actual);
    }
    
    
}
