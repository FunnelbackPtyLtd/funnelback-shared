package com.funnelback.publicui.test.search.model.curator.trigger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.Keys;
import com.funnelback.common.system.EnvironmentVariableException;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.curator.config.Trigger;
import com.funnelback.publicui.search.model.curator.trigger.ExactQueryTrigger;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class ExactQueryTriggerTests {

    @Test
    public void testExactQueryTrigger() throws EnvironmentVariableException, FileNotFoundException {
        ExactQueryTrigger eqt = new ExactQueryTrigger();
        
        SearchQuestion question = new SearchQuestion();
        
        Config config = mock(Config.class);
        when(config.value(Keys.ModernUI.Curator.QUERY_PARAMETER_PATTERN)).thenReturn("^query$");
        question.setCollection(new Collection("test-collection", config));
        
        SearchTransaction st = new SearchTransaction(question, null);
        
        eqt.setTriggerQuery("a B c");
        eqt.setIgnoreCase(false);
        question.getInputParameterMap().put("query", "a c");
        Assert.assertFalse("Expected to fail because query doesn't match", eqt.activatesOn(st));

        question.getInputParameterMap().put("query", "d a B c");
        Assert.assertFalse("Expected to fail because query doesn't match (even if there's a substring)", eqt.activatesOn(st));

        question.getInputParameterMap().put("query", "a b c");
        Assert.assertFalse("Expected to fail because query doesn't match (different in case)", eqt.activatesOn(st));

        question.getInputParameterMap().put("query", "a B c");
        Assert.assertTrue("Expected to pass because query matches exactly", eqt.activatesOn(st));

        question.getInputParameterMap().put("query", " a B c");
        Assert.assertFalse("Expected to fail because query doesn't match (whitespace at start)", eqt.activatesOn(st));

        question.getInputParameterMap().put("query", "a B c ");
        Assert.assertFalse("Expected to fail because query doesn't match (whitespace at end)", eqt.activatesOn(st));

        eqt.setIgnoreCase(true);
        question.getInputParameterMap().put("query", "a b c");
        Assert.assertTrue("Expected to pass because query matches (case is now ignored)", eqt.activatesOn(st));
    }
    
    @Test
    public void testMultiParamExactQueryTrigger() {
        ExactQueryTrigger eqt = new ExactQueryTrigger();
        
        SearchQuestion question = new SearchQuestion();
        
        Config config = mock(Config.class);
        when(config.value(Keys.ModernUI.Curator.QUERY_PARAMETER_PATTERN)).thenReturn(".*query$");
        question.setCollection(new Collection("test-collection", config));
        
        SearchTransaction st = new SearchTransaction(question, null);
        
        eqt.setTriggerQuery("a B c");
        eqt.setIgnoreCase(false);
        question.getInputParameterMap().put("a_query", "a");
        question.getInputParameterMap().put("b_query", "B c");
        Assert.assertTrue("Expected to pass because query should match after concatenation", eqt.activatesOn(st));
    }

//  Needs to move to a CuratorYamlConfigResourceTest
//    @Test
//    public void testSerializeExactQueryTrigger() {
//        ExactQueryTrigger eqt = new ExactQueryTrigger();
//        eqt.setTriggerQuery("uniquequery");
//
//        String yaml = CuratorYamlConfigResource.getYamlObject().dumpAsMap(eqt);
//        Assert.assertTrue("", yaml.contains("uniquequery"));
//    }
}
