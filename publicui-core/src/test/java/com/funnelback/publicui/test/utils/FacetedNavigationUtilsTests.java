package com.funnelback.publicui.test.utils;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.collection.Profile;
import com.funnelback.publicui.utils.FacetedNavigationUtils;

public class FacetedNavigationUtilsTests {
    
    private static final File SEARCH_HOME = new File("src/test/resources/dummy-search_home");

    private Collection c;
    
    @Before
    public void before() throws IOException {
        Profile p1 = new Profile("p1");
        p1.setFacetedNavConfConfig(new FacetedNavigationConfig("p1-conf", null));
        p1.setFacetedNavLiveConfig(new FacetedNavigationConfig("p1-live", null));
        Profile p2 = new Profile("p1");
        p2.setFacetedNavConfConfig(new FacetedNavigationConfig("p2-conf", null));
        p2.setFacetedNavLiveConfig(new FacetedNavigationConfig("p2-live", null));
        
        c = new Collection("dummy", new NoOptionsConfig(SEARCH_HOME, "dummy"));
        c.setFacetedNavigationLiveConfig(new FacetedNavigationConfig("live", null));
        c.setFacetedNavigationConfConfig(new FacetedNavigationConfig("conf", null));
        c.getProfiles().put("p1", p1);
        c.getProfiles().put("p2", p2);
    }
    
    @Test
    public void testNoCollectionConfig() {
        Assert.assertNull(FacetedNavigationUtils.selectConfiguration(new Collection("dummy", null), null));
    }
    
    @Test
    public void testNoFnConfig() {
        c.setFacetedNavigationConfConfig(null);
        c.setFacetedNavigationLiveConfig(null);
        Assert.assertNull(FacetedNavigationUtils.selectConfiguration(c, null));
    }
    
    @Test
    public void testNoOverride() throws Exception {
        Assert.assertEquals("live",
            FacetedNavigationUtils.selectConfiguration(c, null).getQpOptions());
    }

    @Test
    public void testOverride() throws Exception {
        c.getConfiguration().setValue(
            Keys.FacetedNavigation.CONFIG_LOCATION,
            "conf");

        Assert.assertEquals("conf",
            FacetedNavigationUtils.selectConfiguration(c, null).getQpOptions());
    }
    
    @Test
    public void testProfileNoOverride() {
        c.setFacetedNavigationConfConfig(null);
        c.setFacetedNavigationLiveConfig(null);
        
        Assert.assertEquals("p1-live",
            FacetedNavigationUtils.selectConfiguration(c, "p1").getQpOptions());
        Assert.assertEquals("p2-live",
            FacetedNavigationUtils.selectConfiguration(c, "p2").getQpOptions());
        Assert.assertNull(FacetedNavigationUtils.selectConfiguration(c, "invalid"));
    }

    @Test
    public void testProfileOverride() {
        c.getConfiguration().setValue(
            Keys.FacetedNavigation.CONFIG_LOCATION,
            "conf");
        c.setFacetedNavigationConfConfig(null);
        c.setFacetedNavigationLiveConfig(null);
        
        Assert.assertEquals("p1-conf",
            FacetedNavigationUtils.selectConfiguration(c, "p1").getQpOptions());
        Assert.assertEquals("p2-conf",
            FacetedNavigationUtils.selectConfiguration(c, "p2").getQpOptions());
    }

}
