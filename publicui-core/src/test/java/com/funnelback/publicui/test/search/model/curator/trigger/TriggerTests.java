package com.funnelback.publicui.test.search.model.curator.trigger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.curator.config.Trigger;
import com.funnelback.publicui.search.model.curator.trigger.ExactQueryTrigger;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class TriggerTests {

    @Test
    public void testInvalidParamPattern() {
        ExactQueryTrigger eqt = new ExactQueryTrigger();
        
        SearchQuestion question = new SearchQuestion();
        
        Config config = mock(Config.class);
        when(config.value(Keys.ModernUI.Curator.QUERY_PARAMETER_PATTERN, 
            DefaultValues.ModernUI.Curator.QUERY_PARAMETER_PATTERN))
            .thenReturn(".*qu(ery$");
        question.setCollection(new Collection("test-collection", config));
        
        SearchTransaction st = new SearchTransaction(question, null);
        question.getInputParameterMap().put("a_query", "a");
        question.getInputParameterMap().put("b_query", "B c");
        
        Assert.assertEquals("Expected empty response when pattern is invalid.", "", Trigger.queryToMatchAgainst(st));
    }

}
