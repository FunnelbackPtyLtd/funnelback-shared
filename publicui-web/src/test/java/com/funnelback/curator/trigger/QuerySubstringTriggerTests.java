package com.funnelback.curator.trigger;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.resource.impl.CuratorConifgResource;

public class QuerySubstringTriggerTests {

    @Test
    public void testAllQueryWordsTrigger() {
        QuerySubstringTrigger qst = new QuerySubstringTrigger();
        
        SearchQuestion question = new SearchQuestion();
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
    public void testSerializeAllQueryWordsTrigger() {
        QuerySubstringTrigger qst = new QuerySubstringTrigger();
        qst.setTriggerSubstring("uniqueword");
        
        String yaml = CuratorConifgResource.getYamlObject().dumpAsMap(qst);
        Assert.assertTrue("", yaml.contains("uniqueword"));
    }
}
