package com.funnelback.publicui.test.search.lifecycle.input.processors;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import javax.annotation.Resource;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.ExtraSearches;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.test.mock.MockConfigRepository;
import com.funnelback.publicui.test.search.lifecycle.input.processors.extrasearches.ChangeQueryQuestionFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class ExtraSearchesTest {

    private ExtraSearches processor;
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private File searchHome;
    
    @Autowired
    private MockConfigRepository configRepository;
    
    @Resource(name="localConfigRepository")
    private ConfigRepository testConfigRepository;
    
    private SearchTransaction st;
    
    @Before
    public void before() throws IOException {
        processor = new ExtraSearches();
        processor.setApplicationContext(applicationContext);
        processor.setConfigRepository(configRepository);
        
        configRepository.addCollection(testConfigRepository.getCollection("extra-searches"));
        configRepository.addCollection(testConfigRepository.getCollection("extra-search-test1"));
        configRepository.addCollection(testConfigRepository.getCollection("extra-search-test2"));
        
        configRepository.addExtraSearchConfiguration("extra-searches", "test1",
            testConfigRepository.getExtraSearchConfiguration(configRepository.getCollection("extra-searches"), "test1"));
        configRepository.addExtraSearchConfiguration("extra-searches", "test2",
            testConfigRepository.getExtraSearchConfiguration(configRepository.getCollection("extra-searches"), "test2"));
        
        
        Collection c = configRepository.getCollection("extra-searches");
        SearchQuestion question = new SearchQuestion();
        question.setCollection(c);
        st = new SearchTransaction(question, null);
    }
    
    @Test
    public void testMissingData() throws InputProcessorException, FileNotFoundException {
        // No transaction
        processor.processInput(null);
        
        // No question
        processor.processInput(new SearchTransaction(null, null));
        
        // No collection
        SearchQuestion question = new SearchQuestion();
        SearchTransaction st = new SearchTransaction(question, null);
        processor.processInput(st);
        Assert.assertEquals(0, st.getExtraSearchesQuestions().size());
        
        // No query
        question.setCollection(new Collection("dummy", new NoOptionsConfig(searchHome, "dummy")));
        processor.processInput(new SearchTransaction(question, null));
        Assert.assertEquals(0, st.getExtraSearchesQuestions().size());
    }
    
    @Test
    public void test() throws InputProcessorException {
        processor.processInput(st);
        
        Assert.assertEquals(2, st.getExtraSearchesQuestions().size());
        Assert.assertNotNull(st.getExtraSearchesQuestions().get("test1"));
        Assert.assertNotNull(st.getExtraSearchesQuestions().get("test2"));
        
        Assert.assertEquals("extra-search-test1", st.getExtraSearchesQuestions().get("test1").getCollection().getId());
        Assert.assertEquals("extra-search-test2", st.getExtraSearchesQuestions().get("test2").getCollection().getId());        
    }
    
    @Test
    public void testQuestionFactory() throws InputProcessorException {
        Map<String, String> m = testConfigRepository.getExtraSearchConfiguration(configRepository.getCollection("extra-searches"), "test1");
        m.put("class", ChangeQueryQuestionFactory.class.getName());
        
        configRepository.addExtraSearchConfiguration("extra-searches", "test1", m);

        processor.processInput(st);
        
        Assert.assertEquals(2, st.getExtraSearchesQuestions().size());
        Assert.assertNotNull(st.getExtraSearchesQuestions().get("test1"));
        Assert.assertNotNull(st.getExtraSearchesQuestions().get("test2"));
        
        Assert.assertEquals("extra-searches", st.getExtraSearchesQuestions().get("test1").getCollection().getId());
        Assert.assertEquals("HAS BEEN CHANGED", st.getExtraSearchesQuestions().get("test1").getQuery());
        Assert.assertEquals("extra-search-test2", st.getExtraSearchesQuestions().get("test2").getCollection().getId());        
    }

    
}
