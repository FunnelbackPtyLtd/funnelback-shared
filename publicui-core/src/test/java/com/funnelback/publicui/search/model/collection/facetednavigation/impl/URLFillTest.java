package com.funnelback.publicui.search.model.collection.facetednavigation.impl;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.publicui.search.model.collection.QueryProcessorOption;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class URLFillTest {

    private URLFill category;
    
    @Before
    public void before() {
        category = new URLFill("http://example.org/");
        category.setFacetName("facetName");
        category.setLabel("label");
    }
    
    @Test
    public void testFields() {
        Assert.assertEquals("http://example.org/", category.getData());
        Assert.assertEquals("v", category.getMetadataClass());
        Assert.assertEquals("facetName", category.getFacetName());
        Assert.assertEquals("label", category.getLabel());
        
        Assert.assertEquals(Sets.newHashSet("f.facetName|url"), category.getAllQueryStringParamNames());
    }
    
    @Test
    public void testConstraint() {
        Assert.assertEquals("v:\"http://example.org/about\"", category.getQueryConstraint("http://example.org/about"));
    }
    
    @Test
    public void testQPOs() {
        SearchQuestion question = new SearchQuestion();
        question.getSelectedFacets().add("facetName");
        question.getSelectedCategoryValues().put("f.facetName|url", Lists.newArrayList("https://example.org/products"));
        
        List<QueryProcessorOption<?>> actual = category.getQueryProcessorOptions(question);
        Assert.assertEquals(Collections.singletonList(new QueryProcessorOption<>("count_urls", 2)), actual);
    }

    @Test
    public void testQPOsMultipleValues() {
        SearchQuestion question = new SearchQuestion();
        question.getSelectedFacets().add("facetName");
        question.getSelectedCategoryValues().put("f.facetName|url", Lists.newArrayList("https://example.org/products",
            "example.org/about/company", "example.org/produts/vacuum-cleaners/bagless/dyson"));

        List<QueryProcessorOption<?>> actual = category.getQueryProcessorOptions(question);
        Assert.assertEquals(Collections.singletonList(new QueryProcessorOption<>("count_urls", 5)), actual);
    }
    
    @Test
    public void testCountSegments() {
        Assert.assertEquals(1, URLFill.countSegments("https://example.org"));
        Assert.assertEquals(1, URLFill.countSegments("https://example.org/"));
        Assert.assertEquals(1, URLFill.countSegments("http://example.org"));
        Assert.assertEquals(1, URLFill.countSegments("http://example.org/"));
        Assert.assertEquals(1, URLFill.countSegments("example.org"));
        Assert.assertEquals(1, URLFill.countSegments("example.org/"));

        Assert.assertEquals(1, URLFill.countSegments("https://example.org/about"));
        Assert.assertEquals(1, URLFill.countSegments("https://example.org/about/"));
        Assert.assertEquals(1, URLFill.countSegments("http://example.org/about"));
        Assert.assertEquals(1, URLFill.countSegments("http://example.org/about/"));
        Assert.assertEquals(1, URLFill.countSegments("example.org/about"));
        Assert.assertEquals(1, URLFill.countSegments("example.org/about/"));

        Assert.assertEquals(4, URLFill.countSegments("https://example.org/about/products/vacuum-cleaners/bagless"));
        Assert.assertEquals(4, URLFill.countSegments("https://example.org/about/products/vacuum-cleaners/bagless/"));

        Assert.assertEquals(4, URLFill.countSegments("http://example.org/about/products/vacuum-cleaners/bagless"));
        Assert.assertEquals(4, URLFill.countSegments("http://example.org/about/products/vacuum-cleaners/bagless/"));

        Assert.assertEquals(4, URLFill.countSegments("example.org/about/products/vacuum-cleaners/bagless"));
        Assert.assertEquals(4, URLFill.countSegments("example.org/about/products/vacuum-cleaners/bagless/"));
}
    
}
