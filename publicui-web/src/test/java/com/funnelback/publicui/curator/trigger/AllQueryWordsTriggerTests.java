package com.funnelback.publicui.curator.trigger;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.curator.trigger.AllQueryWordsTrigger;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.resource.impl.CuratorConifgResource;

public class AllQueryWordsTriggerTests {

    @Test
    public void testAllQueryWordsTrigger() {
        AllQueryWordsTrigger aqwt = new AllQueryWordsTrigger(Arrays.asList(new String[]{"a", "b"}));
        
        SearchQuestion question = new SearchQuestion();
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
    
    @Test
    public void testSerializeAllQueryWordsTrigger() {
        AllQueryWordsTrigger aqwt = new AllQueryWordsTrigger(Arrays.asList(new String[]{"uniqueword"}));

        String yaml = CuratorConifgResource.getYamlObject().dumpAsMap(aqwt);
        Assert.assertTrue("", yaml.contains("uniqueword"));
    }
}
