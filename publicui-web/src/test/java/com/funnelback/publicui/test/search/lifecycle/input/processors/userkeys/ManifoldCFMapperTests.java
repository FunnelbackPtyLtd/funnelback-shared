package com.funnelback.publicui.test.search.lifecycle.input.processors.userkeys;

import java.io.FileNotFoundException;

import net.sf.ehcache.CacheManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.common.system.EnvironmentVariableException;
import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.UserKeys;
import com.funnelback.publicui.search.lifecycle.input.processors.userkeys.ManifoldCFMapper;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class ManifoldCFMapperTests {

    @Autowired
    private I18n i18n;
    
    @Autowired
    private AutowireCapableBeanFactory beanFactory;
    
    @Autowired
    private CacheManager appCacheManager;

    private UserKeys processor;
    private Collection c;
    
    @Before
    public void before() throws FileNotFoundException, EnvironmentVariableException {
        processor = new UserKeys();
        processor.setI18n(i18n);
        processor.setBeanFactory(beanFactory);
        processor.setAppCacheManager(appCacheManager);
        
        c = new Collection("dummy", 
            new NoOptionsConfig("dummy")
                .setValue(Keys.SecurityEarlyBinding.USER_TO_KEY_MAPPER, ManifoldCFMapper.class.getName())
                .setValue(Keys.ManifoldCF.DOMAIN, "harness.local")
            );
    }

    @Test
    public void testUsernameRewriting() throws InputProcessorException {
        SearchQuestion question = new SearchQuestion();
        question.setCollection(c);
        question.getRawInputParameters().put("REMOTE_USER", new String[]{"HARNESS\\administrator"});
        SearchTransaction st = new SearchTransaction(question, null);

        try {
            processor.processInput(st);
        } catch (Exception e) {
            if (! e.getMessage().contains("administrator@harness.local")) {
                Assert.fail("Expected username 'administrator@harness.local' to appear in the URL (shown in the exception message), but it had " + e.getMessage());
            }
            
            return;
        }
        Assert.fail("Expected an exception to be thrown, but none was.");

        Assert.assertEquals(0, st.getQuestion().getUserKeys().size());
    }

}
