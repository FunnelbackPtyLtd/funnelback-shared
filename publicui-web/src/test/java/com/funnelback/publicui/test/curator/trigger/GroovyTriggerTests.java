package com.funnelback.publicui.test.curator.trigger;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.curator.SimpleGroovyTriggerResourceManager;
import com.funnelback.publicui.curator.trigger.GroovyTrigger;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class GroovyTriggerTests {

    @Test
    public void testGroovyTrigger() {
        GroovyTrigger gt = new GroovyTrigger();
        gt.setClassFile("src/test/resources/curator/TestTrigger.groovy");
        gt.setGroovyTriggerResourceManager(new SimpleGroovyTriggerResourceManager());

        SearchQuestion question = new SearchQuestion();
        SearchTransaction st = new SearchTransaction(question, null);
        
        question.setQuery("triggertrue");
        Assert.assertTrue("Expected groovy to return true", gt.activatesOn(st));

        question.setQuery("triggerfalse");
        Assert.assertFalse("Expected groovy to return false", gt.activatesOn(st));
    }
}
