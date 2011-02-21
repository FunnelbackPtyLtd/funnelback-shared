package com.funnelback.publicui.test.search.lifecycle.input.processors;

import java.io.FileNotFoundException;
import java.util.HashMap;

import junit.framework.Assert;

import org.junit.Test;

import com.funnelback.common.EnvironmentVariableException;
import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.QuickLinks;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class QuickLinksTests {

	private QuickLinks processor = new QuickLinks();
	
	@Test
	public void testMissingData() throws InputProcessorException {
		// No transaction
		processor.process(null, null);
		
		// No question
		processor.process(new SearchTransaction(null, null), null);
		
		// No collection
		SearchQuestion question = new SearchQuestion();
		SearchTransaction st = new SearchTransaction(question, null);
		processor.process(st, null);
		Assert.assertEquals(0, st.getQuestion().getDynamicQueryProcessorOptions().size());
		
		// No quicklinks config
		question.setCollection(new Collection("dummy", null));
		processor.process(new SearchTransaction(question, null), null);
		Assert.assertEquals(0, st.getQuestion().getDynamicQueryProcessorOptions().size());

		// Empty quicklinks config
		Collection c = new Collection("dummy", null);
		c.setQuickLinksConfiguration(new HashMap<String, String>());
		question.setCollection(c);
		processor.process(new SearchTransaction(question, null), null);
		Assert.assertEquals(0, st.getQuestion().getDynamicQueryProcessorOptions().size());

		// quicklinks disabled
		c.getQuickLinksConfiguration().put(Keys.QuickLinks.QUICKLINKS, "disabled");		
		question.setCollection(c);
		processor.process(new SearchTransaction(question, null), null);
		Assert.assertEquals(0, st.getQuestion().getDynamicQueryProcessorOptions().size());

	}
	
	@Test
	public void testWithConfig() throws InputProcessorException, FileNotFoundException, EnvironmentVariableException {
		HashMap<String, String> qlConfig = new HashMap<String, String>();
		qlConfig.put(Keys.QuickLinks.QUICKLINKS, "enabled");
		qlConfig.put(Keys.QuickLinks.DEPTH, "42");
		qlConfig.put(Keys.QuickLinks.RANK, "666");
		
		Collection c = new Collection("dummy", new NoOptionsConfig("dummy"));
		c.setQuickLinksConfiguration(qlConfig);
		
		SearchQuestion question = new SearchQuestion();
		question.setCollection(c);

		SearchTransaction st = new SearchTransaction(question, null);
		processor.process(st, null);
		Assert.assertEquals(2, st.getQuestion().getDynamicQueryProcessorOptions().size());
		Assert.assertTrue(st.getQuestion().getDynamicQueryProcessorOptions().contains("-QL=42"));
		Assert.assertTrue(st.getQuestion().getDynamicQueryProcessorOptions().contains("-QL_rank=666"));
		
		// Try with conflicting query processor options
		st.getQuestion().getDynamicQueryProcessorOptions().clear();
		c.getConfiguration().setValue(Keys.QUERY_PROCESSOR_OPTIONS, "-stem2 -QL=4 -res=xml -QL_rank=all -something");
		processor.process(st, null);
		Assert.assertEquals(0, st.getQuestion().getDynamicQueryProcessorOptions().size());

		// Try with non-conflicting QP options
		st.getQuestion().getDynamicQueryProcessorOptions().clear();
		c.getConfiguration().setValue(Keys.QUERY_PROCESSOR_OPTIONS, "-stem2 -res=xml -something");
		processor.process(st, null);
		Assert.assertEquals(2, st.getQuestion().getDynamicQueryProcessorOptions().size());
		Assert.assertTrue(st.getQuestion().getDynamicQueryProcessorOptions().contains("-QL=42"));
		Assert.assertTrue(st.getQuestion().getDynamicQueryProcessorOptions().contains("-QL_rank=666"));

		
	}
	
	@Test
	public void testWithoutConfig() throws InputProcessorException, FileNotFoundException, EnvironmentVariableException {
		HashMap<String, String> qlConfig = new HashMap<String, String>();
		qlConfig.put(Keys.QuickLinks.QUICKLINKS, "enabled");
		
		Collection c = new Collection("dummy", new NoOptionsConfig("dummy"));
		c.setQuickLinksConfiguration(qlConfig);
		
		SearchQuestion question = new SearchQuestion();
		question.setCollection(c);

		SearchTransaction st = new SearchTransaction(question, null);
		processor.process(st, null);
		Assert.assertEquals(2, st.getQuestion().getDynamicQueryProcessorOptions().size());
		Assert.assertTrue(st.getQuestion().getDynamicQueryProcessorOptions().contains("-QL=1"));
		Assert.assertTrue(st.getQuestion().getDynamicQueryProcessorOptions().contains("-QL_rank=1"));
		
		// Try with conflicting query processor options
		st.getQuestion().getDynamicQueryProcessorOptions().clear();
		c.getConfiguration().setValue(Keys.QUERY_PROCESSOR_OPTIONS, "-stem2 -QL=4 -res=xml -QL_rank=all -something");
		processor.process(st, null);
		Assert.assertEquals(0, st.getQuestion().getDynamicQueryProcessorOptions().size());

		// Try with non-conflicting QP options
		st.getQuestion().getDynamicQueryProcessorOptions().clear();
		c.getConfiguration().setValue(Keys.QUERY_PROCESSOR_OPTIONS, "-stem2 -res=xml -something");
		processor.process(st, null);
		Assert.assertEquals(2, st.getQuestion().getDynamicQueryProcessorOptions().size());
		Assert.assertTrue(st.getQuestion().getDynamicQueryProcessorOptions().contains("-QL=1"));
		Assert.assertTrue(st.getQuestion().getDynamicQueryProcessorOptions().contains("-QL_rank=1"));

	}
	
}
