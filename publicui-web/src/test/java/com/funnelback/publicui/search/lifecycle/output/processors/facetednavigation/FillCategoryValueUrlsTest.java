package com.funnelback.publicui.search.lifecycle.output.processors.facetednavigation;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.util.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import com.funnelback.common.facetednavigation.models.FacetSelectionType;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.Facet.Category;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.google.common.collect.ImmutableMap;



public class FillCategoryValueUrlsTest {

    @Test
    public void testFillUrls() {
        FillCategoryValueUrls fillUrls = new FillCategoryValueUrls(){
            @Override
            Map<String, List<String>> getSelectUrlMap(SearchTransaction st, FacetDefinition fd, CategoryValue categoryValue,
                List<Facet.Category> siblings) {
                return ImmutableMap.of("s", asList("select"));
            }
            
            @Override
            Map<String, List<String>> getUnselectUrlMap(SearchTransaction st, Facet.Category category, CategoryValue categoryValue) {
                return ImmutableMap.of("s", asList("unselect"));
            }
        };
        
        
        CategoryValue categoryValueSelected = mock(CategoryValue.class);
        when(categoryValueSelected.isSelected()).thenReturn(true);
        
        CategoryValue categoryValueUnselected = mock(CategoryValue.class);
        when(categoryValueUnselected.isSelected()).thenReturn(false);
        
        Category cat = mock(Category.class);
        when(cat.getValues()).thenReturn(asList(categoryValueSelected, categoryValueUnselected));
        
        fillUrls.fillURLs(null, null, null, cat);
        
        verify(categoryValueSelected, times(1)).setSelectUrl("?s=select");
        verify(categoryValueSelected, times(1)).setUnselectUrl("?s=unselect");
        verify(categoryValueSelected, times(1)).setToggleUrl(("?s=unselect"));
        
        verify(categoryValueUnselected, times(1)).setSelectUrl("?s=select");
        verify(categoryValueUnselected, times(1)).setUnselectUrl("?s=unselect");
        verify(categoryValueUnselected, times(1)).setToggleUrl("?s=select");
    }
    
    @Test
    public void testGetSelectUrlSingleURL() {
        Facet.Category sibling = mock(Facet.Category.class);
        when(sibling.getQueryStringParamName()).thenReturn("fac|bb");
        
        Facet.Category nephew = mock(Facet.Category.class);
        when(nephew.getQueryStringParamName()).thenReturn("fac|bbchild");
        
        when(sibling.getCategories()).thenReturn(asList(nephew));
        List<Facet.Category> siblings = asList(sibling);
        
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), new SearchResponse());
        st.getQuestion().setQueryStringMap(
            ImmutableMap.<String, List<String>>builder()
            .put("a", asList("b")) 
            .put("fac|bb", asList("set by sibling"))
            .put("fac|bbchild", asList("set child of sibling"))
            .put("facetScope", asList("f.Facet%7CZ=value1&fac%7Ca=a:foobar"))
            .put("fac|a", asList("a:foo", "a:bar"))
            .put("start_rank", asList("12"))
            .build()
            );
        
        CategoryValue catVal = mock(CategoryValue.class);
        when(catVal.getQueryStringParamName()).thenReturn("fac|a");
        when(catVal.getQueryStringParamValue()).thenReturn("plop");
        
        FacetDefinition facetDef = mock(FacetDefinition.class);
        when(facetDef.getSelectionType()).thenReturn(FacetSelectionType.SINGLE);
        
        Map<String, List<String>> result = new FillCategoryValueUrls().getSelectUrlMap(st, facetDef, catVal, siblings);
        
        Assert.assertTrue("Map should still have unrelated params", result.containsKey("a"));
        Assert.assertTrue("Map should still have unrelated params even in facet scope", result.containsKey("facetScope"));
        Assert.assertEquals("f.Facet%7CZ=value1", result.get("facetScope").get(0));
        
        Assert.assertFalse("Should have unset siblings", result.containsKey("fac|bb"));
        Assert.assertFalse("Should have unset children of our siblings", result.containsKey("fac|bbchild"));
        
        Assert.assertEquals("Other values for this facet should have been unselected, and only"
            + " one should be selected.", 1, result.get("fac|a").size());
        Assert.assertEquals("We should have selected the plop value",
            "plop", result.get("fac|a").get(0));
        
        Assert.assertFalse("start_rank should be removed we don't want to end up at a zero result page", 
            result.containsKey("start_rank"));
    }
    
    @Test
    public void testGetSelectUrlMultiSelectURL() {
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), new SearchResponse());
        st.getQuestion().setQueryStringMap(ImmutableMap.of(
            "a", asList("b"), 
            "facetScope", asList("f.Facet%7CZ=value1&fac%7Ca=a:foobar"),
            "fac|a", asList("a:foo", "a:bar")));
        
        CategoryValue catVal = mock(CategoryValue.class);
        when(catVal.getQueryStringParamName()).thenReturn("fac|a");
        when(catVal.getQueryStringParamValue()).thenReturn("plop");
        
        FacetDefinition facetDef = mock(FacetDefinition.class);
        when(facetDef.getSelectionType()).thenReturn(FacetSelectionType.MULTIPLE);
        
        Map<String, List<String>> result = new FillCategoryValueUrls().getSelectUrlMap(st, facetDef, catVal, null);
        
        Assert.assertTrue("Map should still have unrelated params", result.containsKey("a"));
        Assert.assertTrue("Map should still have unrelated params even in facet scope", result.containsKey("facetScope"));
        Assert.assertTrue("Should keep already selected values for this facet.",
             result.get("facetScope").get(0).contains("fac%7Ca=a%3Afoobar"));
        
        Assert.assertTrue("Should keep unrelated param within facet scope.",
                result.get("facetScope").get(0).contains("f.Facet%7CZ=value1"));
        
        Assert.assertEquals("Should have kept all other values for this facet, should have:"
            + "a:foo, a:bar and plop", 3, result.get("fac|a").size());
        
        Assert.assertTrue("should have the facet value a:bar", result.get("fac|a").contains("a:foo"));
        Assert.assertTrue("should have the facet value a:bar", result.get("fac|a").contains("a:bar"));
        Assert.assertTrue("should have the facet value plop", result.get("fac|a").contains("plop"));
    }
    
    @Test
    public void testGetUnselectUrlNoChildren() {
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), new SearchResponse());
        st.getQuestion().setQueryStringMap(ImmutableMap.of(
            "a", asList("b"),
            "facetScope", asList("f.Facet%7CZ=value1&fac%7Ca=a:bar&fac%7Ca=foo"),
            "fac|a", asList("a:foo", "a:bar"),
            "start_rank", asList("12")));

        // When we unselect this facet we only unselect where both key and value match
        // this is probbaly going to work with multi select facets.
        
        Category cat = mock(Category.class);
        when(cat.getCategories()).thenReturn(Collections.emptyList());
        
        CategoryValue catVal = mock(CategoryValue.class);
        when(catVal.getQueryStringParamName()).thenReturn("fac|a");
        when(catVal.getQueryStringParamValue()).thenReturn("a:bar");
        
        Map<String, List<String>> result = new FillCategoryValueUrls().getUnselectUrlMap(st, cat, catVal);
        
        Assert.assertTrue("Map should still have unrelated params", result.containsKey("a"));
        Assert.assertTrue("Map should still have unrelated params even in facet scope", result.containsKey("facetScope"));
        
        Assert.assertTrue("expect the unrelated facetScope option to be left alone",
            result.get("facetScope").get(0).contains("f.Facet%7CZ=value1"));
        Assert.assertTrue("Should only remove values where both the key and value match, the value does not"
            + " match in this case.",
            result.get("facetScope").get(0).contains("fac%7Ca=foo"));
        Assert.assertEquals("Should have exactly two key value pairs left in facetScope",
            2, StringUtils.countMatches(result.get("facetScope").get(0), "=") );
        
        
        Assert.assertEquals("Another value in this facet was selected, do no unset it.",
            "a:foo", result.get("fac|a").get(0));
        
        Assert.assertEquals("We should have unset this facet category value's key and value", 
            1, result.get("fac|a").size());
        
        Assert.assertFalse("start_rank should be removed we don't want to end up at a zero result page", 
            result.containsKey("start_rank"));
    }
    
    
    @Test
    public void testGetUnselectUrlWithChildren() {
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), new SearchResponse());
        st.getQuestion().setQueryStringMap(ImmutableMap.of(
            "a", asList("b"),
            "fac|ga", asList("anything"),
            "facetScope", asList("f.Facet%7CZ=value1&fac%7Ca=a:bar&fac%7Ca=foo"),
            "fac|a", asList("a:foo", "a:bar")));
        
        
        CategoryValue catVal = mock(CategoryValue.class);
        when(catVal.getQueryStringParamName()).thenReturn("fac|a");
        when(catVal.getQueryStringParamValue()).thenReturn("a:bar");
        
        CategoryValue catValGrandChild = mock(CategoryValue.class);
        when(catValGrandChild.getQueryStringParamName()).thenReturn("fac|ga");
        when(catValGrandChild.getQueryStringParamValue()).thenReturn("ga:bar");
        
        Category cat = mock(Category.class);
        Category catChild = mock(Category.class);
        Category catGrandChild = mock(Category.class);
        
        when(cat.getCategories()).thenReturn(asList(catChild));
        when(catChild.getCategories()).thenReturn(asList(catGrandChild));
        
        when(cat.getValues()).thenReturn(asList(catVal));
        when(catGrandChild.getValues()).thenReturn(asList(catValGrandChild));
        
        
        
        Map<String, List<String>> result = new FillCategoryValueUrls().getUnselectUrlMap(st, cat, catVal);
        
        Assert.assertTrue("Map should still have unrelated params", result.containsKey("a"));
        Assert.assertTrue("Map should still have unrelated params even in facet scope", result.containsKey("facetScope"));
        
        Assert.assertTrue("expect the unrelated facetScope option to be left alone",
            result.get("facetScope").get(0).contains("f.Facet%7CZ=value1"));
        Assert.assertTrue("Should only remove values where both the key and value match, the value does not"
            + " match in this case.",
            result.get("facetScope").get(0).contains("fac%7Ca=foo"));
        Assert.assertEquals("Should have exactly two key value pairs left in facetScope",
            2, StringUtils.countMatches(result.get("facetScope").get(0), "=") );
        
        
        Assert.assertEquals("Another value in this facet was selected, do no unset it.",
            "a:foo", result.get("fac|a").get(0));
        
        Assert.assertEquals("We should have unset this facet category value's key and value", 
            1, result.get("fac|a").size());
        
        // Now check that the children are unselected.
        Assert.assertFalse("Children should have unselected", result.containsKey("fac|ga"));
    }
    
    
}
