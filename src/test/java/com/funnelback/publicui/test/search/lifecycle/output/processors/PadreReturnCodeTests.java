package com.funnelback.publicui.test.search.lifecycle.output.processors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.lifecycle.output.processors.PadreReturnCode;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.test.mock.MockLogService;

public class PadreReturnCodeTests {

	private PadreReturnCode processor;
	
	private MockLogService logService;
	
	private SearchTransaction st;
	
	@Before
	public void before() {
		logService = new MockLogService();
		processor = new PadreReturnCode();
		processor.setLogService(logService);
		
		st = new SearchTransaction(new SearchQuestion(), new SearchResponse());
		st.getQuestion().setCollection(new Collection("dummy", null));
	}
	
	@Test
	public void testMissingData() throws OutputProcessorException {
		// No transaction
		processor.process(null);
		Assert.assertEquals(0, logService.getPublicUiWarnings().size());

		// No question nor response
		processor.process(new SearchTransaction(null, null));
		Assert.assertEquals(0, logService.getPublicUiWarnings().size());
		
		// No question
		processor.process(new SearchTransaction(new SearchQuestion(), null));
		Assert.assertEquals(0, logService.getPublicUiWarnings().size());

		// No return code
		SearchResponse response = new SearchResponse();
		processor.process(new SearchTransaction(null, response));
		Assert.assertEquals(0, logService.getPublicUiWarnings().size());
	}
	
	@Test
	public void testCode0() throws OutputProcessorException {
		st.getResponse().setReturnCode(0);
		processor.process(st);
		Assert.assertEquals(0, logService.getPublicUiWarnings().size());
	}
	
	@Test
	public void testCode199() throws OutputProcessorException {
		st.getResponse().setReturnCode(199);
		processor.process(st);
		Assert.assertEquals(1, logService.getPublicUiWarnings().size());
		Assert.assertTrue(logService.getPublicUiWarnings().get(0).getMessage().contains("Could not log query to collection's query log"));
		Assert.assertEquals("dummy", logService.getPublicUiWarnings().get(0).getCollection().getId());
	}

}
