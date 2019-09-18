package com.funnelback.publicui.search.model.collection;

import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.common.config.Config;


public class CollectionTest {

    @Test
    public void testCloneBuilderCopiesAllFields() {
        // We need the all args constructor here.
        Collection collection = new Collection("id", mock(Config.class), 
            mock(Map.class), 
            mock(FacetedNavigationConfig.class), 
            mock(FacetedNavigationConfig.class), 
            new String[0], 
            mock(List.class), 
            mock(Map.class), 
            CollectionTest.class);
        
        Assert.assertEquals("Check that a unmodified clone eqauls the oringal.",
            collection, collection.cloneBuilder().build());
    }
}
