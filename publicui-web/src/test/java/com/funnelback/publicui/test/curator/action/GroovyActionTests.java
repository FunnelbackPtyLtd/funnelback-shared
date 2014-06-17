package com.funnelback.publicui.test.curator.action;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.curator.SimpleGroovyActionResourceManager;
import com.funnelback.publicui.curator.action.GroovyAction;
import com.funnelback.publicui.search.model.curator.config.Action.Phase;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class GroovyActionTests {

    @Test
    public void testGroovyAction() {
        GroovyAction ga = new GroovyAction();
        ga.setClassFile("src/test/resources/curator/TestAction.groovy");
        ga.setGroovyActionResourceManager(new SimpleGroovyActionResourceManager());
        
        SearchQuestion question = new SearchQuestion();
        SearchTransaction st = new SearchTransaction(question, null);

        Assert.assertTrue("Expected to run in INPUT phase", ga.runsInPhase(Phase.INPUT));
        Assert.assertFalse("Expected not to run in OUTPUT phase", ga.runsInPhase(Phase.OUTPUT));

        question.setQuery("initialquery");
        ga.performAction(st, Phase.INPUT);
        
        Assert.assertEquals("Expected query to be modified to 'modified' by the groovy action.", "modified" , st.getQuestion().getQuery());
    }
}
