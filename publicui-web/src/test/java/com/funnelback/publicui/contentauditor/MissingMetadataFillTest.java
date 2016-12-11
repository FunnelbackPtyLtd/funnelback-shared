package com.funnelback.publicui.contentauditor;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.publicui.search.model.collection.QueryProcessorOption;
import com.google.common.collect.Sets;

public class MissingMetadataFillTest {

    private MissingMetadataFill category;
    
    @Before
    public void before() {
        category = new MissingMetadataFill();
        category.setFacetName("facetName");
        category.setLabel("label");
    }
    
    @Test
    public void testFields() {
        Assert.assertEquals("missing", category.getData());
        Assert.assertEquals("missing", category.getMetadataClass());
        Assert.assertEquals("facetName", category.getFacetName());
        Assert.assertEquals("label", category.getLabel());
        
        Assert.assertEquals(Sets.newHashSet("f.facetName|missing"), category.getAllQueryStringParamNames());
    }
    
    @Test
    public void testConstraint() {
        Assert.assertEquals("-a value:$++", category.getQueryConstraint("a value"));
    }
    
    @Test
    public void testQPOs() {
        List<QueryProcessorOption<?>> actual = category.getQueryProcessorOptions(null);
        Assert.assertEquals(Collections.emptyList(), actual);
    }
    

}
