package com.funnelback.publicui.search.lifecycle.output.processors.facetednavigation;

import static java.util.Arrays.asList;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.google.common.collect.ImmutableMap;

public class FillFacetUrlsTest {

    @Test
    public void testUnselect() {
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);
        st.getQuestion().setQueryStringMap(ImmutableMap.of(
            "unrelated", asList("we"),
            "f.author|bar", asList("foo", "bar"),
            "facetScope", asList("f.author%7Cbar=foo")));
        
        Facet facet = new Facet("author");
        
        new FillFacetUrls().setUnselectAllUrl(facet, st);
        
        Assert.assertEquals("?unrelated=we", facet.getUnselectAllUrl());
        
    }
}
