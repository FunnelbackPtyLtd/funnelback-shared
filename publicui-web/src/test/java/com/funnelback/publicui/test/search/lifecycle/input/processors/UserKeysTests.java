package com.funnelback.publicui.test.search.lifecycle.input.processors;

import java.io.FileNotFoundException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.common.EnvironmentVariableException;
import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.UserKeys;
import com.funnelback.publicui.search.lifecycle.input.processors.userkeys.MasterKeyMapper;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.test.mock.MockUserKeysMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class UserKeysTests {
    
    @Autowired
    private I18n i18n;
    
    @Autowired
    private AutowireCapableBeanFactory beanFactory;
    
    private UserKeys processor;
    
    @Before
    public void before() {
        processor = new UserKeys();
        processor.setI18n(i18n);
        processor.setBeanFactory(beanFactory);
    }
    
    @Test
    public void testMissingData() throws InputProcessorException {
        // No transaction
        processor.processInput(null);
        
        // No question
        processor.processInput(new SearchTransaction(null, null));
        
        // No collection
        SearchQuestion question = new SearchQuestion();
        processor.processInput(new SearchTransaction(question, null));        
    }

    @Test
    public void testCustomMockPlugin() throws InputProcessorException, FileNotFoundException, EnvironmentVariableException {
        Collection c = new Collection("dummy", new NoOptionsConfig("dummy")
            .setValue(Keys.SecurityEarlyBinding.USER_TO_KEY_MAPPER,
                    MockUserKeysMapper.class.getName()));
        
    
        SearchQuestion question = new SearchQuestion();
        question.setCollection(c);
        SearchTransaction st = new SearchTransaction(question, null);
        
        processor.processInput(st);
        
        Assert.assertEquals(1, st.getQuestion().getUserKeys().size());
        Assert.assertEquals(MockUserKeysMapper.class.getSimpleName(), st.getQuestion().getUserKeys().get(0));
    }
    
    @Test
    public void testShippedPlugin() throws InputProcessorException, FileNotFoundException, EnvironmentVariableException {
        Collection c = new Collection("dummy", new NoOptionsConfig("dummy").setValue(
                Keys.SecurityEarlyBinding.USER_TO_KEY_MAPPER, "MasterKey"));

        SearchQuestion question = new SearchQuestion();
        question.setCollection(c);
        SearchTransaction st = new SearchTransaction(question, null);

        processor.processInput(st);

        Assert.assertEquals(1, st.getQuestion().getUserKeys().size());
        Assert.assertEquals(MasterKeyMapper.MASTER_KEY, st.getQuestion().getUserKeys().get(0));
    }
    
    @Test
    public void testNoSecurityPlugin() throws FileNotFoundException, EnvironmentVariableException,
            InputProcessorException {
        Collection c = new Collection("dummy", new NoOptionsConfig("dummy").setValue(
                Keys.SecurityEarlyBinding.USER_TO_KEY_MAPPER, null));

        SearchQuestion question = new SearchQuestion();
        question.setCollection(c);
        SearchTransaction st = new SearchTransaction(question, null);

        processor.processInput(st);

        Assert.assertEquals(0, st.getQuestion().getUserKeys().size());
        
        // Empty (non null) value
        c.getConfiguration().setValue(Keys.SecurityEarlyBinding.USER_TO_KEY_MAPPER, "");
        processor.processInput(st);
        Assert.assertEquals(0, st.getQuestion().getUserKeys().size());
    }
    
    @Test
    public void testInvalidClass() throws FileNotFoundException, EnvironmentVariableException,
            InputProcessorException {
        Collection c = new Collection("dummy", new NoOptionsConfig("dummy").setValue(
                Keys.SecurityEarlyBinding.USER_TO_KEY_MAPPER, "InvalidPlugin"));

        SearchQuestion question = new SearchQuestion();
        question.setCollection(c);
        SearchTransaction st = new SearchTransaction(question, null);

        try {
            processor.processInput(st);
            Assert.fail();
        } catch (InputProcessorException ipe) {
            Assert.assertEquals(ClassNotFoundException.class, ipe.getCause().getClass());
        }
    }

    
}
