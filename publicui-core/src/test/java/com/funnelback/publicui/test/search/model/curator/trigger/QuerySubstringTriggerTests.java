package com.funnelback.publicui.test.search.model.curator.trigger;

import static com.funnelback.config.keys.Keys.FrontEndKeys;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.config.configtypes.service.DefaultServiceConfig;
import com.funnelback.config.configtypes.service.ServiceConfig;
import com.funnelback.config.data.InMemoryConfigData;
import com.funnelback.config.data.environment.NoConfigEnvironment;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Profile;
import com.funnelback.publicui.search.model.curator.trigger.QuerySubstringTrigger;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.google.common.collect.Maps;

public class QuerySubstringTriggerTests {

    private static final String COLLECTION_ID = "test-collection";
    private static final String PROFILE_NAME = "profileName";
    private SearchQuestion question;

    @Test
    public void testQuerySubstringTrigger() {
        Profile profile = new Profile();
        profile.setServiceConfig(new DefaultServiceConfig(new InMemoryConfigData(Maps.newHashMap()), new NoConfigEnvironment()));
        Collection collection = new Collection(COLLECTION_ID, null);
        collection.getProfiles().put(PROFILE_NAME, profile);

        question = new SearchQuestion();
        question.setCollection(collection);
        question.setCurrentProfile(PROFILE_NAME);

        SearchTransaction st = new SearchTransaction(question, null);

        QuerySubstringTrigger qst = new QuerySubstringTrigger();
        qst.setTriggerSubstring("bar");

        question.setQuery("foo");
        Assert.assertFalse("Expected to fail because substring doesn't match", qst.activatesOn(st));

        question.setQuery("bar");
        Assert.assertTrue("Expected to pass because substring matches whole query", qst.activatesOn(st));

        question.setQuery("foo bar foo");
        Assert.assertTrue("Expected to pass because substring matches as a substring", qst.activatesOn(st));
    }

    @Test
    public void testMultiParamQuerySubstringTrigger() {
        ServiceConfig serviceConfig = new DefaultServiceConfig(new InMemoryConfigData(Maps.newHashMap()), new NoConfigEnvironment());
        serviceConfig.set(FrontEndKeys.ModernUi.CURATOR.QUERY_PARAMETER_PATTERN, "^q.*");
        Profile profile = new Profile();
        profile.setServiceConfig(serviceConfig);
        Collection collection = new Collection(COLLECTION_ID, null);
        collection.getProfiles().put(PROFILE_NAME, profile);

        question = new SearchQuestion();
        question.setCollection(collection);
        question.setCurrentProfile(PROFILE_NAME);

        SearchTransaction st = new SearchTransaction(question, null);

        QuerySubstringTrigger qst = new QuerySubstringTrigger();
        qst.setTriggerSubstring("bar");

        question.getInputParameterMap().put("q_first", "first");
        question.getInputParameterMap().put("q_second", "second");
        question.getInputParameterMap().put("q_third", "third");
        question.getInputParameterMap().put("q_fourth", "fourth");
        // Because of key sorting, should come out as "first fourth second third"

        qst.setTriggerSubstring("thrid fourth");
        Assert.assertFalse("Expected to fail because substring doesn't match", qst.activatesOn(st));

        qst.setTriggerSubstring("second thrid");
        Assert.assertFalse("Expected to pass because substring matches", qst.activatesOn(st));
    }
}
