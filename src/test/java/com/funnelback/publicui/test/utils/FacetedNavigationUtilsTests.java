package com.funnelback.publicui.test.utils;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.collection.Profile;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.utils.FacetedNavigationUtils;
import com.google.common.collect.ImmutableMap;

public class FacetedNavigationUtilsTests {

    
    @Test
    public void testFromProfile() {
        Collection c = mock(Collection.class);
        String prof = "prof";
        Map<String, Profile> profiles = new HashMap<>();
        profiles.put(prof, mock(Profile.class));
        when(c.getProfiles()).thenReturn(profiles);
        
        FacetedNavigationConfig facetedCfg = mock(FacetedNavigationConfig.class);
        
        when(profiles.get(prof).getFacetedNavConfConfig()).thenReturn(facetedCfg);
        
        Assert.assertEquals(facetedCfg, FacetedNavigationUtils.selectConfiguration(c, prof));        
    }
    
    @Test
    public void testFromLiveIdx() {
        Collection c = mock(Collection.class);
        
        when(c.getProfiles()).thenReturn(new HashMap<>());
        
        FacetedNavigationConfig facetedCfg = mock(FacetedNavigationConfig.class);
        
        when(c.getFacetedNavigationLiveConfig()).thenReturn(facetedCfg);
        
        Assert.assertEquals(facetedCfg, FacetedNavigationUtils.selectConfiguration(c, "p"));
    }
    
    
    @Test
    public void testFromCollectionConfig() {
        Collection c = mock(Collection.class);
        
        when(c.getProfiles()).thenReturn(new HashMap<>());
        
        FacetedNavigationConfig facetedCfg = mock(FacetedNavigationConfig.class);
        
        when(c.getFacetedNavigationConfConfig()).thenReturn(facetedCfg);
        
        Assert.assertEquals(facetedCfg, FacetedNavigationUtils.selectConfiguration(c, null));
    }
    
    @Test
    public void testIsCategorySelected() {
        CategoryDefinition cDef = Mockito.mock(CategoryDefinition.class);
        Mockito.when(cDef.getQueryStringCategoryExtraPart()).thenReturn("data");
        Mockito.when(cDef.getFacetName()).thenReturn("myfacet");
        
        Map<String, List<String>> selectedCategories = new HashMap<>();
        selectedCategories.put("f.myfacet|data", Arrays.asList(new String[] {"value1"}));
        selectedCategories.put("Another|Data", Arrays.asList(new String[] {"value2"}));
        
        Assert.assertTrue(FacetedNavigationUtils.isCategorySelected(cDef, selectedCategories, "value1"));
        Assert.assertFalse(FacetedNavigationUtils.isCategorySelected(cDef, selectedCategories, "value2"));
    }
    
    @Test
    public void testIsFacetSelected() {
        CategoryDefinition cDef = Mockito.mock(CategoryDefinition.class);
        Mockito.when(cDef.getFacetName()).thenReturn("myfacet");
        
        Map<String, List<String>> selectedCategories = new HashMap<>();
        selectedCategories.put("f.myfacet|data", Arrays.asList(new String[] {"value1"}));
        
        Assert.assertTrue(FacetedNavigationUtils.isFacetSelected(cDef, selectedCategories));
    }
    
    @Test
    public void testIsFacetSelectedFacetNotSelected() {
        CategoryDefinition cDef = Mockito.mock(CategoryDefinition.class);
        Mockito.when(cDef.getFacetName()).thenReturn("covfefe");
        
        Map<String, List<String>> selectedCategories = new HashMap<>();
        selectedCategories.put("f.myfacet|data", Arrays.asList(new String[] {"value1"}));
        
        Assert.assertFalse(FacetedNavigationUtils.isFacetSelected(cDef, selectedCategories));
    }
    
    @Test
    public void testRemoveQueryStringFacetValueNothingLeftOver() {
        Map<String, List<String>> queryStringMap = new HashMap<>(
            ImmutableMap.of("a", toList("foo"),
                            "facetScope", toList("a=foo")));
        
        FacetedNavigationUtils.removeQueryStringFacetValue(queryStringMap, "a", "foo");
        
        Assert.assertEquals("All values are empty lists so the key should be removed as "
            + "well resilting in a empty map.",
            0, queryStringMap.size());
    }
    
    @Test
    public void testRemoveQueryStringFacetValue() {
        Map<String, List<String>> queryStringMap = new HashMap<>(
            ImmutableMap.of("a", toList("foo"),
                            "facetScope", toList("a=foo&a=bar")));
        
        FacetedNavigationUtils.removeQueryStringFacetValue(queryStringMap, "a", "foo");
        
        Assert.assertEquals("a=bar does not match the value and so should be kept",
            1, queryStringMap.size());
        
        Assert.assertEquals("a=bar", queryStringMap.get("facetScope").get(0));
    }
    
    @Test
    public void testEmptyMap() {
        Map<String, List<String>> queryStringMap = new HashMap<>();
        
        FacetedNavigationUtils.removeQueryStringFacetValue(queryStringMap, "a", "foo");
        
        Assert.assertEquals("Map should stay empty", 0, queryStringMap.size());
    }
    
    @Test
    public void testRemoveQueryStringFacetKeyNothingLeftOver() {
        Map<String, List<String>> queryStringMap = new HashMap<>(
            ImmutableMap.of("a", toList("foo", "foobar"),
                            "facetScope", toList("a=foo&a=bar")));
        
        FacetedNavigationUtils.removeQueryStringFacetKey(queryStringMap, "a");
        
        Assert.assertEquals("All values are empty lists so the key should be removed as "
            + "well resilting in a empty map.",
            0, queryStringMap.size());
    }
    
    public List<String> toList(String ... vals) {
        return new ArrayList<>(asList(vals));
    }

}
