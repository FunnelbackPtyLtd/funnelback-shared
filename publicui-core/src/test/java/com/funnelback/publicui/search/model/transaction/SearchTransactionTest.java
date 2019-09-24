package com.funnelback.publicui.search.model.transaction;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.transaction.SearchQuestion.SearchQuestionType;

public class SearchTransactionTest {

    @Test
    public void testEffectiveExtraSearch() {
        SearchTransaction parent = stOfType(SearchQuestionType.SEARCH);
        SearchTransaction extra = stOfType(SearchQuestionType.EXTRA_SEARCH);
        extra.setExtraSearchNameAndParentTransaction(Optional.of("alldoc"), Optional.of(parent));
        SearchTransaction extraFacet = stOfType(SearchQuestionType.FACETED_NAVIGATION_EXTRA_SEARCH);
        extraFacet.setExtraSearchNameAndParentTransaction(Optional.of("facet"), Optional.of(extra));
        
        SearchTransaction facet = stOfType(SearchQuestionType.FACETED_NAVIGATION_EXTRA_SEARCH);
        facet.setExtraSearchNameAndParentTransaction(Optional.of("facet"), Optional.of(parent));
        
        Assert.assertEquals(Optional.empty(), parent.getEffecitveExtraSearchName());
        Assert.assertEquals(Optional.empty(), facet.getEffecitveExtraSearchName());
        Assert.assertEquals(Optional.of("alldoc"), extra.getEffecitveExtraSearchName());
        Assert.assertEquals(Optional.of("alldoc"), extraFacet.getEffecitveExtraSearchName());
        
    }
    
    private SearchTransaction stOfType(SearchQuestionType type) {
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), new SearchResponse());
        st.getQuestion().setQuestionType(type);
        return st;
    }
}
