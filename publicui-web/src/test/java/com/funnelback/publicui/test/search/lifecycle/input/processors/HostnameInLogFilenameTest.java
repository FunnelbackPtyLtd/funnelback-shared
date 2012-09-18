package com.funnelback.publicui.test.search.lifecycle.input.processors;

import java.io.File;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.HostnameInLogFilename;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.utils.web.LocalHostnameHolder;

public class HostnameInLogFilenameTest {

	private LocalHostnameHolder localHostnameHolder;
	private HostnameInLogFilename processor;
	private SearchTransaction st;
	
	@Before
	public void before() {
		localHostnameHolder = new LocalHostnameHolder();
		processor = new HostnameInLogFilename();
		processor.setLocalHostnameHolder(localHostnameHolder);
		st = new SearchTransaction(new SearchQuestion(), new SearchResponse());
	}
	
	@Test
	public void testMissingData() throws InputProcessorException {
		// No transaction
		processor.processInput(null);
		
		// No question
		processor.processInput(new SearchTransaction(null, null));
		
		// No collection
		processor.processInput(new SearchTransaction(new SearchQuestion(), null));
	}
	
	@Test
	public void testHostname() throws InputProcessorException {
		st.getQuestion().setCollection(
				new Collection("dummy",
						new NoOptionsConfig("dummy").setValue(Keys.Logging.HOSTNAME_IN_FILENAME, "true")));
		
		Assert.assertEquals(0, st.getQuestion().getDynamicQueryProcessorOptions().size());
		processor.processInput(st);
		
		String expectedFilename = new File(
				st.getQuestion().getCollection().getConfiguration().getCollectionRoot()
				+ File.separator + "live" + File.separator + "log",
				"queries-" + localHostnameHolder.getShortHostname() + ".log").getAbsolutePath();
		
		Assert.assertEquals(1, st.getQuestion().getDynamicQueryProcessorOptions().size());
		Assert.assertEquals("-qlog_file="+expectedFilename, st.getQuestion().getDynamicQueryProcessorOptions().get(0));
	}
	
	@Test
	public void testNoHostname() throws InputProcessorException {
		st.getQuestion().setCollection(
				new Collection("dummy",
						new NoOptionsConfig("dummy").setValue(Keys.Logging.HOSTNAME_IN_FILENAME, "false")));
		
		Assert.assertEquals(st.getQuestion().getDynamicQueryProcessorOptions().size(), 0);
		processor.processInput(st);
		
		Assert.assertEquals(st.getQuestion().getDynamicQueryProcessorOptions().size(), 0);
	}

	
}
