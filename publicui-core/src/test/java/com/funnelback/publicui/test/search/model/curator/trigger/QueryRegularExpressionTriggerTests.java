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
import com.funnelback.publicui.search.model.curator.trigger.ExactQueryTrigger;
import com.funnelback.publicui.search.model.curator.trigger.QueryRegularExpressionTrigger;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class QueryRegularExpressionTriggerTests {

    @Test
    public void testSimpleCases() {
        QueryRegularExpressionTrigger qret = new QueryRegularExpressionTrigger();
        
        SearchQuestion question = new SearchQuestion();
        
        Config config = mock(Config.class);
        when(config.value(Keys.ModernUI.Curator.QUERY_PARAMETER_PATTERN)).thenReturn("^query$");
        question.setCollection(new Collection("test-collection", config));
        
        SearchTransaction st = new SearchTransaction(question, null);
        
        qret.setTriggerPattern("(?i)\\bFoo\\b");

        question.getInputParameterMap().put("query", "Food");
        Assert.assertFalse("Expected to fail because regex doesn't match", qret.activatesOn(st));

        question.getInputParameterMap().put("query", "foo");
        Assert.assertTrue("Expected to pass because regex matches", qret.activatesOn(st));
    }

    @Test
    public void testInvalidRegex() {
        QueryRegularExpressionTrigger qret = new QueryRegularExpressionTrigger();
        
        SearchQuestion question = new SearchQuestion();
        
        Config config = mock(Config.class);
        when(config.value(Keys.ModernUI.Curator.QUERY_PARAMETER_PATTERN)).thenReturn("^query$");
        question.setCollection(new Collection("test-collection", config));
        
        SearchTransaction st = new SearchTransaction(question, null);
        
        qret.setTriggerPattern("(?i)\\b(Foo\\b");

        question.getInputParameterMap().put("query", "foo");
        Assert.assertFalse("Expected to fail because regex is invalid (but should not throw an exception)", qret.activatesOn(st));
    }

    @Test
    public void testMultiParamRegularExpressionTrigger() throws EnvironmentVariableException, FileNotFoundException {
        QueryRegularExpressionTrigger qret = new QueryRegularExpressionTrigger();
        
        SearchQuestion question = new SearchQuestion();
        
        Config config = mock(Config.class);
        when(config.value(Keys.ModernUI.Curator.QUERY_PARAMETER_PATTERN)).thenReturn("^query.*");
        question.setCollection(new Collection("test-collection", config));
        
        SearchTransaction st = new SearchTransaction(question, null);
        
        qret.setTriggerPattern("(?i)\\bo\\sS\\b");

        question.getInputParameterMap().put("query_or", "O");
        question.getInputParameterMap().put("query_sand", "S");
        Assert.assertTrue("Expected to pass because regex matches", qret.activatesOn(st));
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
