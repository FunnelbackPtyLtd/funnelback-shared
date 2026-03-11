package com.funnelback.publicui.search.model.transaction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SearchResponseTest {

    @Test
    public void getFacetByNameTestNoFacets() {
        SearchResponse sr = new SearchResponse();
        Assertions.assertNull(sr.getFacetByName("a"));
        Assertions.assertTrue(sr.getFacets().isEmpty());
    }
    
    @Test
    public void getFacetByNameTestNoFacetWithGivenName() {
        SearchResponse sr = new SearchResponse();
        sr.getFacets().add(new Facet("foo"));
        Assertions.assertNull(sr.getFacetByName("bar"));
        Assertions.assertEquals(1, sr.getFacets().size());
    }
    
    @Test
    public void getFacetByNameTestFacetExists() {
        SearchResponse sr = new SearchResponse();
        sr.getFacets().add(new Facet("bar"));
        Facet fooFacet = new Facet("foo");
        sr.getFacets().add(fooFacet);
        Assertions.assertEquals(fooFacet, sr.getFacetByName("foo"));
        Assertions.assertEquals(2, sr.getFacets().size());
    }
    
    @Test
    public void getFacetByNameTestWithNullName() {
        SearchResponse sr = new SearchResponse();
        sr.getFacets().add(new Facet("foo"));
        Assertions.assertNull(sr.getFacetByName(null));
    }
    
    @Test
    public void getFacetByNameTestWithEmptyName() {
        SearchResponse sr = new SearchResponse();
        sr.getFacets().add(new Facet("foo"));
        Assertions.assertNull(sr.getFacetByName(""));
    }
    
    @Test
    public void getFacetByNameTestWithDuplicateNames() {
        SearchResponse sr = new SearchResponse();
        Facet firstFoo = new Facet("foo");
        Facet secondFoo = new Facet("foo");
        sr.getFacets().add(firstFoo);
        sr.getFacets().add(secondFoo);
        Facet found = sr.getFacetByName("foo");
        Assertions.assertNotNull(found);
        Assertions.assertTrue(found == firstFoo || found == secondFoo);
    }
}