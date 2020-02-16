package com.funnelback.publicui.test.search.model.curator.trigger;

import static com.funnelback.config.keys.Keys.FrontEndKeys;

import org.junit.Assert;
import org.junit.Test;

import java.io.FileNotFoundException;

import com.funnelback.common.system.EnvironmentVariableException;
import com.funnelback.config.configtypes.service.DefaultServiceConfig;
import com.funnelback.config.configtypes.service.ServiceConfig;
import com.funnelback.config.data.InMemoryConfigData;
import com.funnelback.config.data.environment.NoConfigEnvironment;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Profile;
import com.funnelback.publicui.search.model.curator.trigger.ExactQueryTrigger;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.google.common.collect.Maps;

public class ExactQueryTriggerTests {

    private static final String COLLECTION_ID = "test-collection";
    private static final String PROFILE_NAME = "profileName";
    private SearchQuestion question;

    @Test
    public void testExactQueryTrigger() throws EnvironmentVariableException, FileNotFoundException {
        Profile profile = new Profile();
        profile.setServiceConfig(new DefaultServiceConfig(new InMemoryConfigData(Maps.newHashMap()), new NoConfigEnvironment()));
        Collection collection = new Collection(COLLECTION_ID, null);
        collection.getProfiles().put(PROFILE_NAME, profile);

        question = new SearchQuestion();
        question.setCollection(collection);
        question.setCurrentProfile(PROFILE_NAME);
        
        ExactQueryTrigger eqt = new ExactQueryTrigger();
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
        Assert.assertTrue("Expected to pass because whitespace at the start is ignored", eqt.activatesOn(st));

        question.setQuery("a B c ");
        Assert.assertTrue("Expected to pass because whitespace at the end is ignored", eqt.activatesOn(st));

        question.setQuery("a B  \t c");
        Assert.assertTrue("Expected to pass because multiple whitespace characters are collapsed", eqt.activatesOn(st));

        eqt.setIgnoreCase(true);
        question.setQuery("a b c");
        Assert.assertTrue("Expected to pass because query matches (case is now ignored)", eqt.activatesOn(st));
    }
    
    @Test
    public void testMultiParamExactQueryTrigger() {
        ServiceConfig serviceConfig = new DefaultServiceConfig(new InMemoryConfigData(Maps.newHashMap()), new NoConfigEnvironment());
        serviceConfig.set(FrontEndKeys.ModernUi.CURATOR.QUERY_PARAMETER_PATTERN, ".*query$");
        Profile profile = new Profile();
        profile.setServiceConfig(serviceConfig);
        Collection collection = new Collection(COLLECTION_ID, null);
        collection.getProfiles().put(PROFILE_NAME, profile);

        question = new SearchQuestion();
        question.setCollection(collection);
        question.setCurrentProfile(PROFILE_NAME);

        ExactQueryTrigger eqt = new ExactQueryTrigger();
        SearchTransaction st = new SearchTransaction(question, null);
        
        eqt.setTriggerQuery("a B c");
        eqt.setIgnoreCase(false);
        question.getInputParameterMap().put("a_query", "a");
        question.getInputParameterMap().put("b_query", "B c");
        Assert.assertTrue("Expected to pass because query should match after concatenation", eqt.activatesOn(st));
    }
}
