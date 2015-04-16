package com.funnelback.publicui.test.search.model.curator.trigger;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.curator.trigger.CgiParameterTrigger;
import com.funnelback.publicui.search.model.curator.trigger.StringMatchType;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class CgiParameterTriggerTests {

    @Test
    public void testCgiParameterTrigger() {
        CgiParameterTrigger cpt = new CgiParameterTrigger("paramname", "paramvalue", StringMatchType.EXACT);
        
        SearchQuestion question = new SearchQuestion();
        SearchTransaction st = new SearchTransaction(question, null);
        
        question.getRawInputParameters().remove("paramname");
        Assert.assertFalse("Expected to fail because param is not set", cpt.activatesOn(st));

        question.getRawInputParameters().put("paramname", new String[]{});
        Assert.assertFalse("Expected to fail because param is empty", cpt.activatesOn(st));

        question.getRawInputParameters().put("paramname", new String[]{"foo"});
        Assert.assertFalse("Expected to fail because paramvalue does not match", cpt.activatesOn(st));

        question.getRawInputParameters().put("paramname", new String[]{"paramvalue"});
        Assert.assertTrue("Expected to succeed because paramvalue matches", cpt.activatesOn(st));

        question.getRawInputParameters().put("paramname", new String[]{"foo", "paramvalue"});
        Assert.assertTrue("Expected to succeed even though paramvalue is second", cpt.activatesOn(st));
    }
}
