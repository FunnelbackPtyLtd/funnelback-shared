package com.funnelback.publicui.test.search.model.curator.trigger;

import static com.funnelback.config.keys.Keys.FrontEndKeys;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;

import com.funnelback.common.system.EnvironmentVariableException;
import com.funnelback.config.configtypes.service.DefaultServiceConfig;
import com.funnelback.config.configtypes.service.ServiceConfig;
import com.funnelback.config.data.InMemoryConfigData;
import com.funnelback.config.data.environment.NoConfigEnvironment;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Profile;
import com.funnelback.publicui.search.model.curator.trigger.QueryRegularExpressionTrigger;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.google.common.collect.Maps;

public class QueryRegularExpressionTriggerTests {

    private static final String COLLECTION_ID = "test-collection";
    private static final String PROFILE_NAME = "profileName";
    private SearchQuestion question;

    @Before
    public void before() throws Exception {
        Profile profile = new Profile();
        profile.setServiceConfig(new DefaultServiceConfig(new InMemoryConfigData(Maps.newHashMap()), new NoConfigEnvironment()));
        Collection collection = new Collection(COLLECTION_ID, null);
        collection.getProfiles().put(PROFILE_NAME, profile);

        question = new SearchQuestion();
        question.setCollection(collection);
        question.setCurrentProfile(PROFILE_NAME);
    }

    @Test
    public void testSimpleCases() {
        SearchTransaction st = new SearchTransaction(question, null);
        
        QueryRegularExpressionTrigger qret = new QueryRegularExpressionTrigger();
        qret.setTriggerPattern("(?i)\\bFoo\\b");

        question.setQuery("Food");
        Assert.assertFalse("Expected to fail because regex doesn't match", qret.activatesOn(st));

        question.setQuery("foo");
        Assert.assertTrue("Expected to pass because regex matches", qret.activatesOn(st));
    }

    @Test
    public void testInvalidRegex() {
        SearchTransaction st = new SearchTransaction(question, null);
        
        QueryRegularExpressionTrigger qret = new QueryRegularExpressionTrigger();
        qret.setTriggerPattern("(?i)\\b(Foo\\b");

        question.setQuery("foo");
        Assert.assertFalse("Expected to fail because regex is invalid (but should not throw an exception)", qret.activatesOn(st));
    }

    @Test
    public void testMultiParamRegularExpressionTrigger() throws EnvironmentVariableException, FileNotFoundException {
        ServiceConfig serviceConfig = new DefaultServiceConfig(new InMemoryConfigData(Maps.newHashMap()), new NoConfigEnvironment());
        serviceConfig.set(FrontEndKeys.ModernUi.CURATOR.QUERY_PARAMETER_PATTERN, "^query.*");
        Profile profile = new Profile();
        profile.setServiceConfig(serviceConfig);
        Collection collection = new Collection(COLLECTION_ID, null);
        collection.getProfiles().put(PROFILE_NAME, profile);

        question = new SearchQuestion();
        question.setCollection(collection);
        question.setCurrentProfile(PROFILE_NAME);

        SearchTransaction st = new SearchTransaction(question, null);
        
        QueryRegularExpressionTrigger qret = new QueryRegularExpressionTrigger();
        qret.setTriggerPattern("(?i)\\bo\\sS\\b");

        question.getInputParameterMap().put("query_or", "O");
        question.getInputParameterMap().put("query_sand", "S");
        Assert.assertTrue("Expected to pass because regex matches", qret.activatesOn(st));
    }
}
