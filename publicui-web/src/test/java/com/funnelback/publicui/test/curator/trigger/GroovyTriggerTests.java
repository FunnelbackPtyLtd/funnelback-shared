package com.funnelback.publicui.test.curator.trigger;

import lombok.Setter;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.publicui.curator.trigger.GroovyTrigger;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.springmvc.service.resource.ResourceManager;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class GroovyTriggerTests {

    @Autowired
    @Setter
    protected ResourceManager resourceManager;
    
    @Test
    public void testGroovyTrigger() {
        GroovyTrigger gt = new GroovyTrigger();
        gt.setClassFile("src/test/resources/curator/TestTrigger.groovy");
        gt.setResourceManager(resourceManager);
        
        SearchQuestion question = new SearchQuestion();
        SearchTransaction st = new SearchTransaction(question, null);
        
        question.setQuery("triggertrue");
        Assert.assertTrue("Expected groovy to return true", gt.activatesOn(st, null));

        question.setQuery("triggerfalse");
        Assert.assertFalse("Expected groovy to return false", gt.activatesOn(st, null));
    }
}
