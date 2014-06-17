package com.funnelback.publicui.test.curator.trigger;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.curator.trigger.ExactQueryTrigger;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.resource.impl.CuratorYamlConfigResource;

public class ExactQueryTriggerTests {

    @Test
    public void testExactQueryTrigger() {
        ExactQueryTrigger eqt = new ExactQueryTrigger();
        
        SearchQuestion question = new SearchQuestion();
        SearchTransaction st = new SearchTransaction(question, null);
        
        eqt.setTriggerQuery("a B c");
        eqt.setIgnoreCase(false);
        question.setQuery("a c");
        Assert.assertFalse("Expected to fail because query doesn't match", eqt.activatesOn(st));

        question.setQuery("d a B c");
        Assert.assertFalse("Expected to fail because query doesn't match (even if there's a substring)", eqt.activatesOn(st));

        question.setQuery("a b c");
        Assert.assertFalse("Expected to fail because query doesn't match (different in case)", eqt.activatesOn(st));

        question.setQuery("a B c");
        Assert.assertTrue("Expected to pass because query matches exactly", eqt.activatesOn(st));

        question.setQuery(" a B c");
        Assert.assertFalse("Expected to fail because query doesn't match (whitespace at start)", eqt.activatesOn(st));

        question.setQuery("a B c ");
        Assert.assertFalse("Expected to fail because query doesn't match (whitespace at end)", eqt.activatesOn(st));

        eqt.setIgnoreCase(true);
        question.setQuery("a b c");
        Assert.assertTrue("Expected to pass because query matches (case is now ignored)", eqt.activatesOn(st));
    }

    @Test
    public void testSerializeExactQueryTrigger() {
        ExactQueryTrigger eqt = new ExactQueryTrigger();
        eqt.setTriggerQuery("uniquequery");

        String yaml = CuratorYamlConfigResource.getYamlObject().dumpAsMap(eqt);
        Assert.assertTrue("", yaml.contains("uniquequery"));
    }
}
