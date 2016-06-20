package com.funnelback.publicui.test.search.model.curator.trigger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.curator.trigger.AllQueryWordsTrigger;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class AllQueryWordsTriggerTests {

    @Test
    public void testAllQueryWordsTrigger() {
        AllQueryWordsTrigger aqwt = new AllQueryWordsTrigger(Arrays.asList(new String[]{"a", "b"}));
        
        SearchQuestion question = new SearchQuestion();
        
        Config config = mock(Config.class);
        when(config.value(Keys.ModernUI.Curator.QUERY_PARAMETER_PATTERN, 
            DefaultValues.ModernUI.Curator.QUERY_PARAMETER_PATTERN))
            .thenReturn(DefaultValues.ModernUI.Curator.QUERY_PARAMETER_PATTERN);
        question.setCollection(new Collection("test-collection", config));
        
        SearchTransaction st = new SearchTransaction(question, null);
        
        question.setQuery("a c");
        Assert.assertFalse("Expected to fail because b is missing", aqwt.activatesOn(st));

        question.setQuery("b c");
        Assert.assertFalse("Expected to fail because a is missing", aqwt.activatesOn(st));

        question.setQuery("ab");
        Assert.assertFalse("Expected to fail because a and b are missing (as unique words)", aqwt.activatesOn(st));

        question.setQuery("a b");
        Assert.assertTrue("Expected to succeed because a and b are present", aqwt.activatesOn(st));

        question.setQuery("a b c");
        Assert.assertTrue("Expected to succeed because a and b are present (other words don't matter)", aqwt.activatesOn(st));
        
        question.setQuery("c a b");
        Assert.assertTrue("Expected to succeed because a and b are present (other words don't matter)", aqwt.activatesOn(st));

        question.setQuery("c a b d");
        Assert.assertTrue("Expected to succeed because a and b are present (other words don't matter)", aqwt.activatesOn(st));

        question.setQuery("b a");
        Assert.assertTrue("Expected to succeed because a and b are present (order doesn't matter)", aqwt.activatesOn(st));
    }

    /**
     * The UI allows/encourages the entry of 'empty' terms. That's not ideal, but
     * since the user's intent would not be to create a trigger which can never
     * fire, we skip those for matching purposes.
     */
    @Test
    public void testEmptyInputTerms() {
        AllQueryWordsTrigger aqwt = new AllQueryWordsTrigger(Arrays.asList(new String[]{"a", "", " ", "\t"}));
        
        SearchQuestion question = new SearchQuestion();
        
        Config config = mock(Config.class);
        when(config.value(Keys.ModernUI.Curator.QUERY_PARAMETER_PATTERN, 
            DefaultValues.ModernUI.Curator.QUERY_PARAMETER_PATTERN))
            .thenReturn(DefaultValues.ModernUI.Curator.QUERY_PARAMETER_PATTERN);
        question.setCollection(new Collection("test-collection", config));
        
        SearchTransaction st = new SearchTransaction(question, null);
        
        question.setQuery("a");
        Assert.assertTrue("Expected to pass because a is present, and the empty string should be ignored.", aqwt.activatesOn(st));
    }
}
