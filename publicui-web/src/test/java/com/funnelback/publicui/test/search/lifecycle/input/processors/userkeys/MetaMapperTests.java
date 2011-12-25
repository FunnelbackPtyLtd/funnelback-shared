package com.funnelback.publicui.test.search.lifecycle.input.processors.userkeys;

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
import com.funnelback.publicui.search.lifecycle.input.processors.userkeys.MetaCollectionMapper;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Collection.Type;
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
	
	private UserKeys processor;
	
	@Before
	public void before() throws FileNotFoundException, EnvironmentVariableException {
		processor = new UserKeys();
		processor.setI18n(i18n);
		processor.setBeanFactory(beanFactory);
		
		configRepository.addCollection(new Collection("sub1", new NoOptionsConfig("dummy").setValue(
				Keys.SecurityEarlyBinding.USER_TO_KEY_MAPPER, MasterKeyMapper.class.getSimpleName())));
		configRepository.addCollection(new Collection("sub2", new NoOptionsConfig("dummy").setValue(
				Keys.SecurityEarlyBinding.USER_TO_KEY_MAPPER, MasterKeyMapper.class.getSimpleName())));


	}

	@Test
	public void testNotSetToMeta() throws InputProcessorException, FileNotFoundException, EnvironmentVariableException {
		Collection meta = new Collection("meta", new NoOptionsConfig("dummy").setValue(
				Keys.SecurityEarlyBinding.USER_TO_KEY_MAPPER, MasterKeyMapper.class.getSimpleName()));
		meta.setMetaComponents(new String[] {"sub1", "sub2"});
		
		SearchQuestion question = new SearchQuestion();
		question.setCollection(meta);
		SearchTransaction st = new SearchTransaction(question, null);

		processor.processInput(st);

		Assert.assertEquals(1, st.getQuestion().getUserKeys().size());
		Assert.assertEquals(MasterKeyMapper.MASTER_KEY, st.getQuestion().getUserKeys().get(0));
	}
	
	@Test
	public void testSetToMeta() throws InputProcessorException, FileNotFoundException, EnvironmentVariableException {
		Collection meta = new Collection("meta", new NoOptionsConfig("dummy").setValue(
				Keys.SecurityEarlyBinding.USER_TO_KEY_MAPPER, MetaCollectionMapper.class.getSimpleName())
				.setValue(Keys.COLLECTION_TYPE, Type.meta.toString()));
		meta.setMetaComponents(new String[] {"sub1", "sub2"});
		configRepository.addCollection(meta);
		
		SearchQuestion question = new SearchQuestion();
		question.setCollection(meta);
		SearchTransaction st = new SearchTransaction(question, null);

		processor.processInput(st);

		Assert.assertEquals(2, st.getQuestion().getUserKeys().size());
		Assert.assertEquals(MasterKeyMapper.MASTER_KEY, st.getQuestion().getUserKeys().get(0));
		Assert.assertEquals(MasterKeyMapper.MASTER_KEY, st.getQuestion().getUserKeys().get(1));
	}


}
