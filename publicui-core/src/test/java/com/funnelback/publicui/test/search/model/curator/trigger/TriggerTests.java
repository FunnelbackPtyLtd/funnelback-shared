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
import com.funnelback.publicui.search.model.curator.config.Trigger;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.google.common.collect.Maps;

public class TriggerTests {

    private static final String COLLECTION_ID = "test-collection";
    private static final String PROFILE_NAME = "profileName";

    @Test
    public void testInvalidParamPattern() {
        ServiceConfig serviceConfig = new DefaultServiceConfig(new InMemoryConfigData(Maps.newHashMap()), new NoConfigEnvironment());
        serviceConfig.set(FrontEndKeys.ModernUi.CURATOR.QUERY_PARAMETER_PATTERN, ".*qu(ery$");
        Profile profile = new Profile();
        profile.setServiceConfig(serviceConfig);
        Collection collection = new Collection(COLLECTION_ID, null);
        collection.getProfiles().put(PROFILE_NAME, profile);

        SearchQuestion question = new SearchQuestion();
        question.setCollection(collection);
        question.setCurrentProfile(PROFILE_NAME);

        SearchTransaction st = new SearchTransaction(question, null);
        question.getInputParameterMap().put("a_query", "a");
        question.getInputParameterMap().put("b_query", "B c");

        Assert.assertEquals("Expected empty response when pattern is invalid.", "", Trigger.queryToMatchAgainst(st));
    }

}
