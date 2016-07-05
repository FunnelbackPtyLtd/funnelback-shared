package com.funnelback.publicui.test.utils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.collection.Profile;
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
    

}
