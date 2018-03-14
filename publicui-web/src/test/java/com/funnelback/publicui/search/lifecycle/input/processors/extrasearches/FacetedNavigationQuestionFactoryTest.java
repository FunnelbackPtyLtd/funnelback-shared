package com.funnelback.publicui.search.lifecycle.input.processors.extrasearches;

import java.util.HashMap;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.SearchQuestionType;


public class FacetedNavigationQuestionFactoryTest {

    private final FacetedNavigationQuestionFactory facetedNavigationQuestionFactory = new FacetedNavigationQuestionFactory();
    
    @Test
    public void testLogIsSetToOff() {
        SearchQuestion originalQ = new SearchQuestion();
        originalQ.setLogQuery(Optional.ofNullable(false));
        Assert.assertFalse(facetedNavigationQuestionFactory.buildBasicExtraFacetSearch(originalQ).getLogQuery().get());
        
        Assert.assertFalse(facetedNavigationQuestionFactory.buildQuestion(originalQ, new HashMap<>()).getLogQuery().get());
    }
    
    @Test
    public void testSearchQuestioTypeSet() {
        SearchQuestion originalQ = new SearchQuestion();
        originalQ.setQuestionType(SearchQuestionType.SEARCH);
        
        
        Assert.assertEquals(SearchQuestionType.FACETED_NAVIGATION_EXTRA_SEARCH, 
            facetedNavigationQuestionFactory.buildBasicExtraFacetSearch(originalQ).getQuestionType());
        
        Assert.assertEquals(SearchQuestionType.FACETED_NAVIGATION_EXTRA_SEARCH, 
            facetedNavigationQuestionFactory.buildQuestion(originalQ, new HashMap<>()).getQuestionType());
    }
}
