package com.funnelback.publicui.search.model.collection.facetednavigation.impl.urlfill;

import org.junit.Assert;
import org.junit.Test;


public class FacetURLTest {

    @Test
    public void testEqualsAndHashOnComparisonUrl() {
        
        FacetURL facetURL1 = new FacetURL("http://foo.com/a");
        FacetURL facetURL2 = new FacetURL("http://foo.COM/a");
        
        Assert.assertEquals(facetURL1.getUrlForComparison(), facetURL2.getUrlForComparison());
        Assert.assertEquals(facetURL1, facetURL2);
        Assert.assertEquals(facetURL1.hashCode(), facetURL2.hashCode());
    }
    
    @Test
    public void testSmbUrl() {
        FacetURL facetURL = new FacetURL("\\\\foo.COM\\A\\Bar");
        Assert.assertEquals("smb://foo.com/a/bar/", facetURL.getUrlForComparison());
        Assert.assertEquals("smb://foo.COM/A/Bar/", facetURL.getUrlFixed());
    }
    
    @Test
    public void testUrlMissingScheme() {
        FacetURL facetURL = new FacetURL("example.Com/Bar");
        Assert.assertEquals("http://example.com/Bar/", facetURL.getUrlForComparison());
        Assert.assertEquals("http://example.Com/Bar/", facetURL.getUrlFixed());
    }
    
}
