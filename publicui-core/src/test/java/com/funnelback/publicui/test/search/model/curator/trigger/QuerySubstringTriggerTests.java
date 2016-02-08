package com.funnelback.publicui.test.search.model.curator.trigger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.curator.trigger.QuerySubstringTrigger;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class QuerySubstringTriggerTests {

    @Test
    public void testQuerySubstringTrigger() {
        QuerySubstringTrigger qst = new QuerySubstringTrigger();
        
        SearchQuestion question = new SearchQuestion();
        
        Config config = mock(Config.class);
        when(config.value(Keys.ModernUI.Curator.QUERY_PARAMETER_PATTERN, 
            DefaultValues.ModernUI.Curator.QUERY_PARAMETER_PATTERN))
            .thenReturn(DefaultValues.ModernUI.Curator.QUERY_PARAMETER_PATTERN);
        question.setCollection(new Collection("test-collection", config));
        
        SearchTransaction st = new SearchTransaction(question, null);
        
        qst.setTriggerSubstring("bar");

        question.setQuery("foo");
        Assert.assertFalse("Expected to fail because substring doesn't match", qst.activatesOn(st));

        question.setQuery("bar");
        Assert.assertTrue("Expected to pass because substring matches whole query", qst.activatesOn(st));

        question.setQuery("foo bar foo");
        Assert.assertTrue("Expected to pass because substring matches as a substring", qst.activatesOn(st));
    }

    @Test
    public void testMultiParamQuerySubstringTrigger() {
        QuerySubstringTrigger qst = new QuerySubstringTrigger();
        
        SearchQuestion question = new SearchQuestion();
        
        Config config = mock(Config.class);
        when(config.value(Keys.ModernUI.Curator.QUERY_PARAMETER_PATTERN, 
            DefaultValues.ModernUI.Curator.QUERY_PARAMETER_PATTERN))
            .thenReturn("^q.*");
        question.setCollection(new Collection("test-collection", config));
        
        SearchTransaction st = new SearchTransaction(question, null);
        
        qst.setTriggerSubstring("bar");

        question.getInputParameterMap().put("q_first", "first");
        question.getInputParameterMap().put("q_second", "second");
        question.getInputParameterMap().put("q_third", "third");
        question.getInputParameterMap().put("q_fourth", "fourth");
        // Because of key sorting, should come out as "first fourth second third"

        qst.setTriggerSubstring("thrid fourth");
        Assert.assertFalse("Expected to fail because substring doesn't match", qst.activatesOn(st));

        qst.setTriggerSubstring("second thrid");
        Assert.assertFalse("Expected to pass because substring matches", qst.activatesOn(st));
    }
}
