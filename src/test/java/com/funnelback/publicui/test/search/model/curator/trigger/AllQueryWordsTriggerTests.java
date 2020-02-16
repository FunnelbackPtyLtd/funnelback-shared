package com.funnelback.publicui.test.search.model.curator.trigger;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.config.configtypes.service.DefaultServiceConfig;
import com.funnelback.config.data.InMemoryConfigData;
import com.funnelback.config.data.environment.NoConfigEnvironment;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Profile;
import com.funnelback.publicui.search.model.curator.trigger.AllQueryWordsTrigger;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.google.common.collect.Maps;

public class AllQueryWordsTriggerTests {

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
    public void testAllQueryWordsTrigger() {
        AllQueryWordsTrigger aqwt = new AllQueryWordsTrigger(Arrays.asList(new String[]{"a", "b"}));
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

    /**
     * The UI allows/encourages the entry of 'empty' terms. That's not ideal, but
     * since the user's intent would not be to create a trigger which can never
     * fire, we skip those for matching purposes.
     */
    @Test
    public void testEmptyInputTerms() {
        AllQueryWordsTrigger aqwt = new AllQueryWordsTrigger(Arrays.asList(new String[]{"a", "", " ", "\t"}));
        SearchTransaction st = new SearchTransaction(question, null);
        
        question.setQuery("a");
        Assert.assertTrue("Expected to pass because a is present, and the empty string should be ignored.", aqwt.activatesOn(st));
    }
}
