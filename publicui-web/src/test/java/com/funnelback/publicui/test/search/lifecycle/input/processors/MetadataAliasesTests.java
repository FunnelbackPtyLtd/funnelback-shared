package com.funnelback.publicui.test.search.lifecycle.input.processors;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;

import com.funnelback.common.config.Config;
import com.funnelback.common.system.EnvironmentVariableException;
import com.funnelback.config.configtypes.service.DefaultServiceConfig;
import com.funnelback.config.configtypes.service.ServiceConfig;
import com.funnelback.config.data.InMemoryConfigData;
import com.funnelback.config.data.environment.NoConfigEnvironment;
import static com.funnelback.config.keys.Keys.FrontEndKeys;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.MetadataAliases;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Profile;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.google.common.collect.Maps;

public class MetadataAliasesTests {
    
    private MetadataAliases processor;
    private SearchTransaction st;
    
    Config config;
    
    @Before
    public void before() throws EnvironmentVariableException, FileNotFoundException {
        processor = new MetadataAliases();
    }

    @Test
    public void testLink() throws InputProcessorException {
        st = getTestSearchTransaction("f");
        st.getQuestion().setQuery("abc link:http://www.funnelback.com def link:file:///file.txt link :me");
        processor.processInput(st);
        Assert.assertEquals("abc h:http://www.funnelback.com def h:file:///file.txt link :me", st.getQuestion().getQuery());
    }
    
    @Test
    public void testSite() throws InputProcessorException {
        st = getTestSearchTransaction("f");
        st.getQuestion().setQuery("abc site:http://www.funnelback.com def site:file:///file.txt site :me");
        processor.processInput(st);
        Assert.assertEquals("abc u:http://www.funnelback.com def u:file:///file.txt site :me", st.getQuestion().getQuery());
    }
    
    @Test
    public void testIsolatedColon() throws InputProcessorException {
        st = getTestSearchTransaction("f");
        st.getQuestion().setQuery("foo : :bar foo: bar");
        processor.processInput(st);
        Assert.assertEquals("foo : :bar foo: bar", st.getQuestion().getQuery());
    }
    
    @Test
    public void testFiletype() throws InputProcessorException {
        st = getTestSearchTransaction("f");
        st.getQuestion().setQuery("abc filetype:pdf def doc filetype:jpeg filetype :test file type :me");
        processor.processInput(st);
        Assert.assertEquals("abc f:pdf def doc f:jpeg filetype :test file type :me", st.getQuestion().getQuery());
    }
    
    @Test
    public void testFiletypeNull() throws InputProcessorException {
        st = getTestSearchTransaction(null);
        st.getQuestion().setQuery("abc filetype:pdf def doc filetype:jpeg bam:hahaha site:sitesite");
        processor.processInput(st);
        Assert.assertEquals("abc filetype:pdf def doc filetype:jpeg bam:hahaha u:sitesite", st.getQuestion().getQuery());
    }
    
    @Test
    public void testFiletypeEmptyString() throws InputProcessorException {
        st = getTestSearchTransaction("");
        st.getQuestion().setQuery("abc filetype:pdf def doc filetype:jpeg bam:hahaha site:sitesite");
        processor.processInput(st);
        Assert.assertEquals("abc filetype:pdf def doc filetype:jpeg bam:hahaha u:sitesite", st.getQuestion().getQuery());
    }
    
    @Test
    public void testAllInUrl() throws InputProcessorException {
        st = getTestSearchTransaction("f");
        st.getQuestion().setQuery("abc allinurl:test/url def allinurl:test/again/url.ext allin url :test allinurl :me");
        processor.processInput(st);
        Assert.assertEquals("abc v:test/url def v:test/again/url.ext allin url :test allinurl :me", st.getQuestion().getQuery());
    }
    
    @Test
    public void testInvalidParameters() {
        try {
            processor.processInput(null);
            processor.processInput(new SearchTransaction(null, null));
            processor.processInput(new SearchTransaction(new SearchQuestion(), null));
        } catch (Exception e) {
            Assert.fail();
        }
    }
    
    @Test
    public void testEmptyQuery() {
        st = getTestSearchTransaction("f");
        st.getQuestion().setQuery("");
        try {
            processor.processInput(st);
        } catch (Exception e) {
            Assert.fail();
        }
        
        Assert.assertEquals("", st.getQuestion().getQuery());
    }
    
    @Test
    public void testNoOperatorShouldntAffectQuery() throws InputProcessorException {
        String expected = "There is no operator in this query";
        st = getTestSearchTransaction("f");
        st.getQuestion().setQuery(expected);
        processor.processInput(st);
        Assert.assertEquals(expected, st.getQuestion().getQuery());
    }

    private SearchTransaction getTestSearchTransaction(String filetype) {
        ServiceConfig serviceConfig = new DefaultServiceConfig(new InMemoryConfigData(Maps.newHashMap()), new NoConfigEnvironment());
        serviceConfig.set(FrontEndKeys.ModernUi.getMetadataAlias("link"), Optional.of("h"));
        serviceConfig.set(FrontEndKeys.ModernUi.getMetadataAlias("site"), Optional.of("u"));
        serviceConfig.set(FrontEndKeys.ModernUi.getMetadataAlias("filetype"), Optional.ofNullable(filetype));
        serviceConfig.set(FrontEndKeys.ModernUi.getMetadataAlias("allinurl"), Optional.of("v"));

        Profile profile = new Profile();
        profile.setServiceConfig(serviceConfig);

        Collection collection = new Collection("dummy", null);
        collection.getProfiles().put("profile-test", profile);

        SearchQuestion question = new SearchQuestion();
        question.setCollection(collection);
        question.setCurrentProfile("profile-test");

        return new SearchTransaction(question, null);
    }
    
}
