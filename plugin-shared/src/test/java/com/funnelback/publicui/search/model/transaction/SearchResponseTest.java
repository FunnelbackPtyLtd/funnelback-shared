package com.funnelback.publicui.search.model.transaction;

import org.junit.Assert;
import org.junit.Test;

public class SearchResponseTest {

    @Test
    public void getFacetByNameTestNoFacets() {
        SearchResponse sr = new SearchResponse();
        Assert.assertNull(sr.getFacetByName("a"));
    }
    
    @Test
    public void getFacetByNameTestNoFacetWithGivenName() {
        SearchResponse sr = new SearchResponse();
        sr.getFacets().add(new Facet("foo"));
        Assert.assertNull(sr.getFacetByName("bar"));
    }
    
    @Test
    public void getFacetByNameTestFacetExists() {
        SearchResponse sr = new SearchResponse();
        sr.getFacets().add(new Facet("bar"));
        Facet fooFacet = new Facet("foo");
        sr.getFacets().add(fooFacet);
        Assert.assertEquals(fooFacet, sr.getFacetByName("foo"));
    }
}
