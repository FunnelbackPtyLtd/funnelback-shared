package com.funnelback.publicui.accessibilityauditor.lifecycle.input.processors;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.config.configtypes.service.DefaultServiceConfig;
import com.funnelback.config.configtypes.service.ServiceConfig;
import com.funnelback.config.data.InMemoryConfigData;
import com.funnelback.config.data.environment.NoConfigEnvironment;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Profile;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.google.common.collect.Maps;
import freemarker.template.TemplateModel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import static com.funnelback.config.keys.Keys.FrontEndKeys;

public class AccessibilityAuditorDaatOptionTest {

    private ServiceConfig serviceConfig;
    private SearchQuestion searchQuestion;

    @Before
    public void before() throws IOException {

        Config config = new NoOptionsConfig(new File("src/test/resources/dummy-search_home"), "dummy");

        serviceConfig = new DefaultServiceConfig(new InMemoryConfigData(Maps.newHashMap()), new NoConfigEnvironment());

        Profile profile = new Profile("_default");
        profile.setServiceConfig(serviceConfig);
        Collection collection = new Collection("dummy", config);
        collection.getProfiles().put("_default", profile);

        searchQuestion = new SearchQuestion();
        searchQuestion.setCurrentProfile("_default");
        searchQuestion.setCollection(collection);
    }

    @Test
    public void testDefaultDaatOption() {
        String daatOption = new AccessibilityAuditorDaatOption().getDaatOption(searchQuestion);
        Assert.assertEquals("-daat=150000",daatOption);
    }

    @Test
    public void testDaatOption() {
        serviceConfig.set(FrontEndKeys.ModernUi.AccessibilityAuditor.DAAT_LIMIT, 130000);
        String daatOption = new AccessibilityAuditorDaatOption().getDaatOption(searchQuestion);
        Assert.assertEquals("-daat=130000",daatOption);
    }
}
