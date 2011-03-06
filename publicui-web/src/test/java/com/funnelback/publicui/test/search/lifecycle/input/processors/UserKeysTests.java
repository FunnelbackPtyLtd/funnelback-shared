package com.funnelback.publicui.test.search.lifecycle.input.processors;

import java.io.FileNotFoundException;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.common.EnvironmentVariableException;
import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.UserKeys;
import com.funnelback.publicui.search.lifecycle.input.processors.userkeys.MasterKeyMapper;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.test.mock.MockUserKeysMapper;

public class UserKeysTests {
	
	@Test
	public void testMissingData() throws InputProcessorException {
		UserKeys processor = new UserKeys();
		
		// No transaction
		processor.process(null, null);
		
		// No question
		processor.process(new SearchTransaction(null, null), null);
		
		// No collection
		SearchQuestion question = new SearchQuestion();
		processor.process(new SearchTransaction(question, null), null);		
	}

	@Test
	public void testCustomMockPlugin() throws InputProcessorException, FileNotFoundException, EnvironmentVariableException {
		Collection c = new Collection("dummy", new NoOptionsConfig("dummy")
			.setValue(Keys.SecurityEarlyBinding.USER_TO_KEY_MAPPER,
					MockUserKeysMapper.class.getName()));
		
	
		SearchQuestion question = new SearchQuestion();
		question.setCollection(c);
		SearchTransaction st = new SearchTransaction(question, null);
		
		UserKeys uk = new UserKeys();
		uk.process(st, null);
		
		Assert.assertEquals(1, st.getQuestion().getUserKeys().size());
		Assert.assertEquals(MockUserKeysMapper.class.getSimpleName(), st.getQuestion().getUserKeys().get(0));
	}
	
	@Test
	public void testShippedPlugin() throws InputProcessorException, FileNotFoundException, EnvironmentVariableException {
		Collection c = new Collection("dummy", new NoOptionsConfig("dummy").setValue(
				Keys.SecurityEarlyBinding.USER_TO_KEY_MAPPER, MasterKeyMapper.class.getSimpleName()));

		SearchQuestion question = new SearchQuestion();
		question.setCollection(c);
		SearchTransaction st = new SearchTransaction(question, null);

		UserKeys uk = new UserKeys();
		uk.process(st, null);

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

		UserKeys uk = new UserKeys();
		uk.process(st, null);

		Assert.assertEquals(0, st.getQuestion().getUserKeys().size());
		
		// Empty (non null) value
		c.getConfiguration().setValue(Keys.SecurityEarlyBinding.USER_TO_KEY_MAPPER, "");
		uk.process(st, null);
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

		UserKeys uk = new UserKeys();
		
		try {
			uk.process(st, null);
			Assert.fail();
		} catch (InputProcessorException ipe) {
			Assert.assertEquals(ClassNotFoundException.class, ipe.getCause().getClass());
		}
	}

	
}
