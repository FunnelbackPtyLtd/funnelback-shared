package com.funnelback.publicui.test.search.lifecycle.input.processors;

import java.io.FileNotFoundException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.Keys;
import com.funnelback.common.junit.ConfigStub;
import com.funnelback.common.system.EnvironmentVariableException;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.MetadataAliases;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class MetadataAliasesTests {
    
    private MetadataAliases processor;
    private SearchTransaction st;
    
    Config config;
    
    @Before
    public void before() throws EnvironmentVariableException, FileNotFoundException {
        processor = new MetadataAliases();
        st = new SearchTransaction(new SearchQuestion(){
            @Override
            public Collection getCollection() {
                return new Collection(){
                    @Override
                    public Config getConfiguration() {
                        return config;
                    }
                };
            }
        }, null);
        config = new ConfigStub(null);
        config.setValue(Keys.ModernUI.metadataAlias("link"), "h");
        config.setValue(Keys.ModernUI.metadataAlias("site"), "u");
        config.setValue(Keys.ModernUI.metadataAlias("filetype"), "f");
        config.setValue(Keys.ModernUI.metadataAlias("allinurl"), "v");
        
    }

    @Test
    public void testLink() throws InputProcessorException {
        
        st.getQuestion().setQuery("abc link:http://www.funnelback.com def link:file:///file.txt link :me");
        processor.processInput(st);
        Assert.assertEquals("abc h:http://www.funnelback.com def h:file:///file.txt link :me", st.getQuestion().getQuery());
    }
    
    @Test
    public void testSite() throws InputProcessorException {
        st.getQuestion().setQuery("abc site:http://www.funnelback.com def site:file:///file.txt site :me");
        processor.processInput(st);
        Assert.assertEquals("abc u:http://www.funnelback.com def u:file:///file.txt site :me", st.getQuestion().getQuery());
    }
    
    @Test
    public void testFiletype() throws InputProcessorException {
        st.getQuestion().setQuery("abc filetype:pdf def doc filetype:jpeg filetype :test file type :me");
        processor.processInput(st);
        Assert.assertEquals("abc f:pdf def doc f:jpeg filetype :test file type :me", st.getQuestion().getQuery());
    }
    
    @Test
    public void testFiletypeNull() throws InputProcessorException {
        config.setValue(Keys.ModernUI.metadataAlias("filetype"), null);
        st.getQuestion().setQuery("abc filetype:pdf def doc filetype:jpeg bam:hahaha site:sitesite");
        processor.processInput(st);
        Assert.assertEquals("abc filetype:pdf def doc filetype:jpeg bam:hahaha u:sitesite", st.getQuestion().getQuery());
    }
    
    @Test
    public void testFiletypeEmptyString() throws InputProcessorException {
        config.setValue(Keys.ModernUI.metadataAlias("filetype"), "");
        st.getQuestion().setQuery("abc filetype:pdf def doc filetype:jpeg bam:hahaha site:sitesite");
        processor.processInput(st);
        Assert.assertEquals("abc filetype:pdf def doc filetype:jpeg bam:hahaha u:sitesite", st.getQuestion().getQuery());
    }
    
    @Test
    public void testAllInUrl() throws InputProcessorException {
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
        st.getQuestion().setQuery(expected);
        processor.processInput(st);
        Assert.assertEquals(expected, st.getQuestion().getQuery());
    }
    
    
}
