package com.funnelback.publicui.search.model.transaction;

import java.util.Optional;

import com.funnelback.publicui.search.model.transaction.SearchQuestion.SearchQuestionType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        
        Assertions.assertEquals(Optional.empty(), parent.getEffectiveExtraSearchName());
        Assertions.assertEquals(Optional.empty(), facet.getEffectiveExtraSearchName());
        Assertions.assertEquals(Optional.of("alldoc"), extra.getEffectiveExtraSearchName());
        Assertions.assertEquals(Optional.of("alldoc"), extraFacet.getEffectiveExtraSearchName());
        
    }
    
    private SearchTransaction stOfType(SearchQuestionType type) {
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), new SearchResponse());
        st.getQuestion().setQuestionType(type);
        return st;
    }
}