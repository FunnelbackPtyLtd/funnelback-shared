package com.funnelback.publicui.test.utils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.DateFieldFill;
import com.funnelback.publicui.utils.FacetedNavigationUtils;

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
        Mockito.when(cDef.getQueryStringParamName()).thenReturn("Name|Data");
        
        Map<String, List<String>> selectedCategories = new HashMap<>();
        selectedCategories.put("Name|Data", Arrays.asList(new String[] {"value1"}));
        selectedCategories.put("Another|Data", Arrays.asList(new String[] {"value2"}));
        
        Assert.assertTrue(FacetedNavigationUtils.isCategorySelected(cDef, selectedCategories, "value1"));
        Assert.assertFalse(FacetedNavigationUtils.isCategorySelected(cDef, selectedCategories, "value2"));
    }
    

}
