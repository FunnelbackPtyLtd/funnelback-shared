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
import com.funnelback.common.config.Collection.Type;
import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.UserKeys;
import com.funnelback.publicui.search.lifecycle.input.processors.userkeys.MasterKeyMapper;
import com.funnelback.publicui.search.lifecycle.input.processors.userkeys.MetaMapper;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.test.mock.MockConfigRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class MetaMapperTests {

    @Autowired
    private I18n i18n;
    
    @Autowired
    private MockConfigRepository configRepository;
    
    @Autowired
    private AutowireCapableBeanFactory beanFactory;
    
    @Autowired
    private CacheManager appCacheManager;
    
    private UserKeys processor;
    
    @Before
    public void before() throws FileNotFoundException, EnvironmentVariableException {
        processor = new UserKeys();
        processor.setI18n(i18n);
        processor.setBeanFactory(beanFactory);
        processor.setAppCacheManager(appCacheManager);
        
        configRepository.addCollection(new Collection("sub1", new NoOptionsConfig("dummy").setValue(
                Keys.SecurityEarlyBinding.USER_TO_KEY_MAPPER, MasterKeyMapper.class.getName())));
        configRepository.addCollection(new Collection("sub2", new NoOptionsConfig("dummy").setValue(
                Keys.SecurityEarlyBinding.USER_TO_KEY_MAPPER, MasterKeyMapper.class.getName())));


    }

    @Test
    public void testNotSetToMeta() throws InputProcessorException, FileNotFoundException, EnvironmentVariableException {
        Collection meta = new Collection("meta", new NoOptionsConfig("dummy").setValue(
                Keys.SecurityEarlyBinding.USER_TO_KEY_MAPPER, MasterKeyMapper.class.getName()));
        meta.setMetaComponents(new String[] {"sub1", "sub2"});
        
        SearchQuestion question = new SearchQuestion();
        question.setCollection(meta);
        SearchTransaction st = new SearchTransaction(question, null);

        processor.processInput(st);

        Assert.assertEquals(1, st.getQuestion().getUserKeys().size());
        Assert.assertEquals("meta;" + MasterKeyMapper.MASTER_KEY, st.getQuestion().getUserKeys().get(0));
    }
    
    @Test
    public void testSetToMeta() throws InputProcessorException, FileNotFoundException, EnvironmentVariableException {
        Collection meta = new Collection("meta", new NoOptionsConfig("dummy").setValue(
                Keys.SecurityEarlyBinding.USER_TO_KEY_MAPPER, MetaMapper.class.getName())
                .setValue(Keys.COLLECTION_TYPE, Type.meta.toString()));
        meta.setMetaComponents(new String[] {"sub1", "sub2"});
        configRepository.addCollection(meta);
        
        SearchQuestion question = new SearchQuestion();
        question.setCollection(meta);
        SearchTransaction st = new SearchTransaction(question, null);

        processor.processInput(st);

        Assert.assertEquals(2, st.getQuestion().getUserKeys().size());
        Assert.assertEquals("sub1;" + MasterKeyMapper.MASTER_KEY, st.getQuestion().getUserKeys().get(0));
        Assert.assertEquals("sub2;" + MasterKeyMapper.MASTER_KEY, st.getQuestion().getUserKeys().get(1));
    }


}
