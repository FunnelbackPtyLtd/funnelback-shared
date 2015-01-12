package com.funnelback.publicui.test.search.lifecycle.input.processors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.MultiFacetedNavigation;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.SearchQuestionType;

public class MultiFacetedNavigationTest {

    private MultiFacetedNavigation processor;
    private SearchTransaction st;
    
    @Before
    public void before() {
        processor = new MultiFacetedNavigation();
        
        Collection c = new Collection("dummy", new NoOptionsConfig("dummy"));
        SearchQuestion question = new SearchQuestion();
        question.setCollection(c);
        st = new SearchTransaction(question, null);
    }
    
    @Test
    public void testMissingData() throws InputProcessorException {
        // No transaction
        processor.processInput(null);
        
        // No question
        processor.processInput(new SearchTransaction(null, null));
        
        // No collection
        SearchQuestion question = new SearchQuestion();
        SearchTransaction st = new SearchTransaction(question, null);
        processor.processInput(st);
        Assert.assertEquals(0, st.getExtraSearchesQuestions().size());
    }
    
    @Test
    public void testDisabled() throws InputProcessorException {
        st.getQuestion().getCollection().getConfiguration()
            .setValue(Keys.ModernUI.FULL_FACETS_LIST, "false");
        
        processor.processInput(st);
        
        Assert.assertEquals(0, st.getExtraSearchesQuestions().size());
    }

    @Test
    public void testEnabled() throws InputProcessorException {
        st.getQuestion().getCollection().getConfiguration()
            .setValue(Keys.ModernUI.FULL_FACETS_LIST, "true");
        
        processor.processInput(st);
        
        Assert.assertEquals(1, st.getExtraSearchesQuestions().size());
        Assert.assertTrue(st.getExtraSearchesQuestions().containsKey(SearchTransaction.ExtraSearches.FACETED_NAVIGATION.toString()));
        Assert.assertNotNull(st.getExtraSearchesQuestions().get(SearchTransaction.ExtraSearches.FACETED_NAVIGATION.toString()));
    }
    
    @Test
    public void testExtraSearch() throws InputProcessorException {
        st.getQuestion().getCollection().getConfiguration()
            .setValue(Keys.ModernUI.FULL_FACETS_LIST, "true");
        st.getQuestion().setQuestionType(SearchQuestionType.EXTRA_SEARCH);
    
        processor.processInput(st);
        
        Assert.assertEquals(0, st.getExtraSearchesQuestions().size());
    }

    
}
