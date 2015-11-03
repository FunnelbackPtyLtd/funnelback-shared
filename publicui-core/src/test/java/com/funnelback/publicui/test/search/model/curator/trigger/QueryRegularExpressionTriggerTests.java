package com.funnelback.publicui.test.search.model.curator.trigger;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.curator.trigger.QueryRegularExpressionTrigger;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class QueryRegularExpressionTriggerTests {

    @Test
    public void testSimpleCases() {
        QueryRegularExpressionTrigger qret = new QueryRegularExpressionTrigger();
        
        SearchQuestion question = new SearchQuestion();
        SearchTransaction st = new SearchTransaction(question, null);
        
        qret.setTriggerPattern("(?i)\\bFoo\\b");

        question.setQuery("Food");
        Assert.assertFalse("Expected to fail because regex doesn't match", qret.activatesOn(st));

        question.setQuery("foo");
        Assert.assertTrue("Expected to pass because regex matches", qret.activatesOn(st));
    }

    @Test
    public void testInvalidRegex() {
        QueryRegularExpressionTrigger qret = new QueryRegularExpressionTrigger();
        
        SearchQuestion question = new SearchQuestion();
        SearchTransaction st = new SearchTransaction(question, null);
        
        qret.setTriggerPattern("(?i)\\b(Foo\\b");

        question.setQuery("foo");
        Assert.assertFalse("Expected to fail because regex is invalid (but should not throw an exception)", qret.activatesOn(st));
    }

//  Needs to move to a CuratorYamlConfigResourceTest
//    @Test
//    public void testSerializeAllQueryWordsTrigger() {
//        QueryRegularExpressionTrigger qret = new QueryRegularExpressionTrigger();
//        qret.setTriggerPattern("(?i)\\buniqueword\\b");
//        
//        String yaml = CuratorYamlConfigResource.getYamlObject().dumpAsMap(qret);
//        Assert.assertTrue("", yaml.contains("uniqueword"));
//    }
}
