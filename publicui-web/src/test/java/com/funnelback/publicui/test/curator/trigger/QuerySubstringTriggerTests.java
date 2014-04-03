package com.funnelback.publicui.test.curator.trigger;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.curator.trigger.QuerySubstringTrigger;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.resource.impl.CuratorYamlConfigResource;

public class QuerySubstringTriggerTests {

    @Test
    public void testQuerySubstringTrigger() {
        QuerySubstringTrigger qst = new QuerySubstringTrigger();
        
        SearchQuestion question = new SearchQuestion();
        SearchTransaction st = new SearchTransaction(question, null);
        
        qst.setTriggerSubstring("bar");

        question.setQuery("foo");
        Assert.assertFalse("Expected to fail because substring doesn't match", qst.activatesOn(st, null));

        question.setQuery("bar");
        Assert.assertTrue("Expected to pass because substring matches whole query", qst.activatesOn(st, null));

        question.setQuery("foo bar foo");
        Assert.assertTrue("Expected to pass because substring matches as a substring", qst.activatesOn(st, null));
    }

    @Test
    public void testSerializeQuerySubstringTrigger() {
        QuerySubstringTrigger qst = new QuerySubstringTrigger();
        qst.setTriggerSubstring("uniqueword");
        
        String yaml = CuratorYamlConfigResource.getYamlObject().dumpAsMap(qst);
        Assert.assertTrue("", yaml.contains("uniqueword"));
    }
}
