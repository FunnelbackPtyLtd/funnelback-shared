package com.funnelback.publicui.search.model.transaction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SearchResponseTest {

    @Test
    public void getFacetByNameTestNoFacets() {
        SearchResponse sr = new SearchResponse();
        Assertions.assertNull(sr.getFacetByName("a"));
    }
    
    @Test
    public void getFacetByNameTestNoFacetWithGivenName() {
        SearchResponse sr = new SearchResponse();
        sr.getFacets().add(new Facet("foo"));
        Assertions.assertNull(sr.getFacetByName("bar"));
    }
    
    @Test
    public void getFacetByNameTestFacetExists() {
        SearchResponse sr = new SearchResponse();
        sr.getFacets().add(new Facet("bar"));
        Facet fooFacet = new Facet("foo");
        sr.getFacets().add(fooFacet);
        Assertions.assertEquals(fooFacet, sr.getFacetByName("foo"));
    }
}