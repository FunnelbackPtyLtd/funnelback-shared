package com.funnelback.publicui.search.model.collection.facetednavigation.impl;

import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.publicui.search.model.collection.QueryProcessorOption;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryValueComputedDataHolder;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
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
        Assert.assertEquals(Collections.singletonList(new QueryProcessorOption<>("count_urls", 4)), actual);
    }

    @Test
    public void testQPOsMultipleValues() {
        SearchQuestion question = new SearchQuestion();
        question.getSelectedFacets().add("facetName");
        question.getSelectedCategoryValues().put("f.facetName|url", Lists.newArrayList("https://example.org/products",
            "example.org/about/company", "example.org/produts/vacuum-cleaners/bagless/dyson"));

        List<QueryProcessorOption<?>> actual = category.getQueryProcessorOptions(question);
        Assert.assertEquals(Collections.singletonList(new QueryProcessorOption<>("count_urls", 1 + 4 + 2)), actual);
    }
    
    
    @Test
    public void testLongURLPrefix() {
        SearchQuestion question = new SearchQuestion();
        question.getSelectedFacets().add("facetName");
        question.getSelectedCategoryValues().put("f.facetName|url", Lists.newArrayList("https://example.org/products"));

        URLFill urlFill = new URLFill("http://foo.com/1/2/3/4/5/6/7/8/9/0");
        List<QueryProcessorOption<?>> actual = urlFill.getQueryProcessorOptions(question);
        Assert.assertEquals("We must have a count_urls depth that is deeper than the URL prefix of the category.",
            Collections.singletonList(new QueryProcessorOption<>("count_urls", 13)), actual);
    }
    
    @Test
    public void testCountSegments() {
        Assert.assertEquals(0, URLFill.countSegments("https://example.org"));
        Assert.assertEquals(0, URLFill.countSegments("https://example.org/"));
        Assert.assertEquals(0, URLFill.countSegments("http://example.org"));
        Assert.assertEquals(0, URLFill.countSegments("http://example.org/"));
        Assert.assertEquals(0, URLFill.countSegments("example.org"));
        Assert.assertEquals(0, URLFill.countSegments("example.org/"));

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
    
   @Test
   public void testGetDepth() {
       Assert.assertEquals(2, URLFill.getDepth("folder1", "smb://server/folder1/folder2/folder3"));
       Assert.assertEquals(1, URLFill.getDepth("folder1/folder2", "smb://server/folder1/folder2/folder3"));
       Assert.assertEquals(0, URLFill.getDepth("folder1/folder2/folder3", "smb://server/folder1/folder2/folder3"));
       
       Assert.assertEquals(-1, URLFill.getDepth("folder1/folder2/folder4", "smb://server/folder1/folder2/folder3/"));
       Assert.assertEquals(-1, URLFill.getDepth("folder0/folder1", "smb://server/folder1/folder2/folder3"));
       Assert.assertEquals(-1, URLFill.getDepth("dummy", "smb://server/folder1/folder2/folder3"));
   }
   
   @Test
   public void testGetSelectedItemsVVSUrls() throws Exception {
       String currentConstraint = "home/luke/Documents";
       String url = "\\\\win.win\\home\\/";
       List<String> items = URLFill.getSelectedItems(currentConstraint, url);
       Assert.assertTrue(items.contains("smb://win.win/home/luke"));
       Assert.assertTrue(items.contains("smb://win.win/home/luke/Documents"));
       Assert.assertEquals(2, items.size());
   }
   
   @Test
   public void testGetSelectedItemsCaseIsIgnored() throws Exception {
       String currentConstraint = "home/luke/Documents";
       String url = "http://win.win/Home";
       List<String> items = URLFill.getSelectedItems(currentConstraint, url);
       Assert.assertTrue(items.contains("win.win/Home/luke"));
       Assert.assertTrue(items.contains("win.win/Home/luke/Documents"));
       Assert.assertEquals(2, items.size());
   }
   
   @Test
   public void testGetSelectedItemsNothingSelected() throws Exception {
       String currentConstraint = "";
       String url = "\\\\win.win\\home\\/";
       List<String> items = URLFill.getSelectedItems(currentConstraint, url);
       Assert.assertEquals(0, items.size());
   }
   
   @Test
   public void testInjectSelectedValues() throws Exception {
       URLFill urlFill = new URLFill("http://foo.com/1/");
       SearchTransaction st = new SearchTransaction(new SearchQuestion(), new SearchResponse());
       st.getResponse().setResultPacket(new ResultPacket());
       
       st.getQuestion().getSelectedCategoryValues().put(urlFill.getQueryStringParamName(), 
           Arrays.asList("1/bar/Fruit"));
       
       Map<String, Integer> urlCounts = st.getResponse().getResultPacket().getUrlCounts();
       
       urlCounts.put("foo.com/1/bar/Fruit", 1);
       urlCounts.put("foo.com/1/bar/Fruit/Apples", 1);
       
       FacetDefinition facetDef = mock(FacetDefinition.class);
       List<CategoryValueComputedDataHolder> result = urlFill.computeData(st, facetDef);
       
       List<String> labels = result.stream().map(c -> c.getLabel()).collect(Collectors.toList());
       Assert.assertTrue(labels.contains("bar"));
       Assert.assertTrue(labels.contains("Fruit"));
       Assert.assertTrue(labels.contains("Apples"));
       Assert.assertEquals(3, labels.size());
   }
    
}
