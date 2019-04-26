package com.funnelback.publicui.test.search.lifecycle.input.processors.userkeys;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.common.system.EnvironmentVariableException;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.lifecycle.input.processors.UserKeys;
import com.funnelback.publicui.search.lifecycle.input.processors.userkeys.ManifoldCFMapper;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

import net.sf.ehcache.CacheManager;

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
            new NoOptionsConfig(new File("src/test/resources/dummy-search_home/"), "dummy")
                .setValue(Keys.SecurityEarlyBinding.USER_TO_KEY_MAPPER, ManifoldCFMapper.class.getName())
                .setValue(Keys.ManifoldCF.DOMAIN, "harness.local")
            );
    }
    
    @Test
    public void testUsernameRewriting() throws Exception {
        Class<?> clazz = Class.forName(ManifoldCFMapper.class.getName());
        ManifoldCFMapper mapper = (ManifoldCFMapper) beanFactory.createBean(clazz);
        
        SearchQuestion question = new SearchQuestion();
        question.setCollection(c);
        question.getRawInputParameters().put("REMOTE_USER", new String[]{"HARNESS\\administrator"});
        SearchTransaction st = new SearchTransaction(question, null);
        
        Assert.assertEquals("administrator@harness.local",mapper.getFullUsername(st));
        
    }

}
