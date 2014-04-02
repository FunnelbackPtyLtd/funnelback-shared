package com.funnelback.publicui.test.curator.trigger;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.curator.trigger.QueryRegularExpressionTrigger;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.resource.impl.CuratorConfigResource;

public class QueryRegularExpressionTriggerTests {

    @Test
    public void testAllQueryWordsTrigger() {
        QueryRegularExpressionTrigger qret = new QueryRegularExpressionTrigger();
        
        SearchQuestion question = new SearchQuestion();
        SearchTransaction st = new SearchTransaction(question, null);
        
        qret.setTriggerPattern("(?i)\\bFoo\\b");

        question.setQuery("Food");
        Assert.assertFalse("Expected to fail because regex doesn't match", qret.activatesOn(st, null));

        question.setQuery("foo");
        Assert.assertTrue("Expected to pass because regex matches", qret.activatesOn(st, null));
    }

    @Test
    public void testSerializeAllQueryWordsTrigger() {
        QueryRegularExpressionTrigger qret = new QueryRegularExpressionTrigger();
        qret.setTriggerPattern("(?i)\\buniqueword\\b");
        
        String yaml = CuratorConfigResource.getYamlObject().dumpAsMap(qret);
        Assert.assertTrue("", yaml.contains("uniqueword"));
    }
}
