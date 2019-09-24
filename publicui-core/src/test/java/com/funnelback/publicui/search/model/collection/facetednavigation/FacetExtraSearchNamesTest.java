package com.funnelback.publicui.search.model.collection.facetednavigation;

import org.junit.Assert;
import org.junit.Test;

public class FacetExtraSearchNamesTest {

    private FacetExtraSearchNames facetExtraSearchNames = new FacetExtraSearchNames();
    
    @Test
    public void testEncoding() {
        String a = facetExtraSearchNames.getExtraSearchName("--", "-", "-");
        String b = facetExtraSearchNames.getExtraSearchName("-", "--", "-");
        Assert.assertNotEquals(a, b);
    }
}
