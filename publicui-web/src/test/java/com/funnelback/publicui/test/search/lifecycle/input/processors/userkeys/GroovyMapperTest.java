package com.funnelback.publicui.test.search.lifecycle.input.processors.userkeys;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ResourceBundleMessageSource;

import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.lifecycle.input.processors.userkeys.GroovyMapper;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class GroovyMapperTest {

    private static final File script = new File("src/test/resources/dummy-search_home/lib/java/groovy/com/funnelback/test/GroovyMapper.groovy");
    
    private GroovyMapper mapper;
    private SearchTransaction transaction;
    
    @Before
    public void before() {
        ResourceBundleMessageSource messages = new ResourceBundleMessageSource();
        messages.setUseCodeAsDefaultMessage(true);
        
        I18n i18n = new I18n();
        i18n.setMessages(messages);
        mapper = new GroovyMapper();
        mapper.setI18n(i18n);
        
        transaction = new SearchTransaction(new SearchQuestion(), null);
        transaction.getQuestion().setCollection(new Collection("dummy",
            new NoOptionsConfig("dummy")
                .setValue(Keys.SecurityEarlyBinding.GROOVY_CLASS, "com.funnelback.test.GroovyMapper")));
        
        script.getParentFile().mkdirs();
    }
    
    @After
    public void after() {
        script.delete();
    }
    
    @Test
    public void testNoGroovyClassName() {
        transaction.getQuestion().getCollection().getConfiguration().setValue(Keys.SecurityEarlyBinding.GROOVY_CLASS, null);
        
        try {
            mapper.getUserKeys(transaction);
            Assert.fail();
        } catch (IllegalArgumentException iae) {
            Assert.assertEquals("inputprocessor.userkeys.groovy.undefined", iae.getMessage());
        }
    }
    
    @Test
    public void testWrongGroovyClass() {
        transaction.getQuestion().getCollection().getConfiguration().setValue(Keys.SecurityEarlyBinding.GROOVY_CLASS, "invalid");
        
        try {
            mapper.getUserKeys(transaction);
            Assert.fail();
        } catch (IllegalArgumentException iae) {
            Assert.assertEquals(ClassNotFoundException.class, iae.getCause().getClass());
        }
    }
    
    @Test
    public void testNullReturn() throws IOException {
        FileUtils.writeStringToFile(script, "return null");
        List<String> l = mapper.getUserKeys(transaction);
        Assert.assertTrue(l.isEmpty());
    }

    @Test
    public void testInvalidReturn() throws IOException {
        FileUtils.writeStringToFile(script, "return 12");
        List<String> l = mapper.getUserKeys(transaction);
        Assert.assertTrue(l.isEmpty());
    }

    @Test
    public void testNoReturn() throws IOException {
        FileUtils.writeStringToFile(script, "def x = 12");
        List<String> l = mapper.getUserKeys(transaction);
        Assert.assertTrue(l.isEmpty());
    }

    @Test
    public void testStringReturn() throws IOException {
        FileUtils.writeStringToFile(script, "return \"user key\"");
        List<String> l = mapper.getUserKeys(transaction);
        
        Assert.assertEquals(1, l.size());
        Assert.assertEquals("user key", l.get(0));
    }

    @Test
    public void testListReturn() throws IOException {
        FileUtils.writeStringToFile(script, "return [\"key 1\", \"key 2\"]");
        List<String> l = mapper.getUserKeys(transaction);
        
        Assert.assertEquals(2, l.size());
        Assert.assertEquals("key 1", l.get(0));
        Assert.assertEquals("key 2", l.get(1));
    }

    @Test
    public void testCompilationError() throws IOException {
        FileUtils.writeStringToFile(script, "invalid");
        List<String> l = mapper.getUserKeys(transaction);
        Assert.assertTrue(l.isEmpty());
    }

    @Test
    public void testExecutionError() throws IOException {
        FileUtils.writeStringToFile(script, "return 12/0");
        List<String> l = mapper.getUserKeys(transaction);
        Assert.assertTrue(l.isEmpty());
    }

}
