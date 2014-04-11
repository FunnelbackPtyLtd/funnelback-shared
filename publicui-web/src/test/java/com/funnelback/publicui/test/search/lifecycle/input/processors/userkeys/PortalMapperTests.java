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
import com.funnelback.publicui.search.lifecycle.input.processors.userkeys.PortalMapper;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class PortalMapperTests {

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
        
        c = new Collection("dummy", new NoOptionsConfig("dummy").setValue(
                Keys.SecurityEarlyBinding.USER_TO_KEY_MAPPER, PortalMapper.class.getName()));
    }

    @Test
    public void testNoHeader() throws InputProcessorException {
        SearchQuestion question = new SearchQuestion();
        question.setCollection(c);
        SearchTransaction st = new SearchTransaction(question, null);

        processor.processInput(st);

        Assert.assertEquals(0, st.getQuestion().getUserKeys().size());
    }
    
    @Test
    public void testHeader() throws InputProcessorException {
        SearchQuestion question = new SearchQuestion();
        question.getRawInputParameters().put(PortalMapper.PORTAL_PARAMETER_NAME, new String[] {"ab,cd,ef"});
        question.setCollection(c);
        SearchTransaction st = new SearchTransaction(question, null);

        processor.processInput(st);

        Assert.assertEquals(3, st.getQuestion().getUserKeys().size());
        Assert.assertEquals("ab", st.getQuestion().getUserKeys().get(0));
        Assert.assertEquals("cd", st.getQuestion().getUserKeys().get(1));
        Assert.assertEquals("ef", st.getQuestion().getUserKeys().get(2));
    }


}
