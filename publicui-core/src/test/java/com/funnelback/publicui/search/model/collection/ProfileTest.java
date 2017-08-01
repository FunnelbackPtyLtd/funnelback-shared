package com.funnelback.publicui.search.model.collection;

import static org.mockito.Mockito.mock;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.config.configtypes.service.ServiceConfigReadOnly;
import com.funnelback.publicui.search.model.curator.config.CuratorConfig;

public class ProfileTest {

    @Test
    public void testCloneBuilderCopiesAllFields() {
        Profile profile = new Profile("foo", 
            mock(FacetedNavigationConfig.class), 
            mock(CuratorConfig.class), 
            mock(ServiceConfigReadOnly.class),
            "padre opts");
        
        Assert.assertEquals("Check that a unmodified clone eqauls the oringal.",
            profile, profile.cloneBuilder().build());
        
    }
}
