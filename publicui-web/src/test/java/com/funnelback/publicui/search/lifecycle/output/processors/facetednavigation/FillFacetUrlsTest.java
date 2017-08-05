package com.funnelback.publicui.search.lifecycle.output.processors.facetednavigation;

import static java.util.Arrays.asList;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.common.facetednavigation.models.FacetConstraintJoin;
import com.funnelback.common.facetednavigation.models.FacetSelectionType;
import com.funnelback.common.facetednavigation.models.FacetValues;
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
            "facetScope", asList("f.author%7Cbar=foo"),
            "start_rank", asList("12")));
        
        Facet facet = new Facet("author", FacetSelectionType.SINGLE,
            FacetConstraintJoin.LEGACY,
            FacetValues.FROM_SCOPED_QUERY);
        
        new FillFacetUrls().setUnselectAllUrl(facet, st);
        
        Assert.assertEquals("?unrelated=we", facet.getUnselectAllUrl());
        
        Assert.assertFalse("start_rank should be removed we don't want to end up at a zero result page", 
            facet.getUnselectAllUrl().contains("start_rank"));
        
    }
}