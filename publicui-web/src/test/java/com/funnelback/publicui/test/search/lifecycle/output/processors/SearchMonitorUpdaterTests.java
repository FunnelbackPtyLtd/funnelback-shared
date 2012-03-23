package com.funnelback.publicui.test.search.lifecycle.output.processors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.publicui.search.jmx.SearchMonitor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.lifecycle.output.processors.SearchMonitorUpdater;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class SearchMonitorUpdaterTests {

	private SearchTransaction st;
	private SearchMonitor monitor;
	private SearchMonitorUpdater processor;
	
	@Before
	public void before() {
		monitor = new SearchMonitor();
		processor = new SearchMonitorUpdater();
		processor.setMonitor(monitor);
		
		st = new SearchTransaction(new SearchQuestion(), new SearchResponse());
		st.getResponse().setResultPacket(new ResultPacket());
	}
	
	@Test
	public void testMissingData() throws Exception {
		// No transaction
		processor.processOutput(null);
		
		// No response & question
		processor.processOutput(new SearchTransaction(null, null));
		
		// No question
		processor.processOutput(new SearchTransaction(null, new SearchResponse()));
		
		// No response
		processor.processOutput(new SearchTransaction(new SearchQuestion(), null));
		
		// No results
		SearchResponse response = new SearchResponse();
		processor.processOutput(new SearchTransaction(null, response));
		
		// No results in packet
		response.setResultPacket(new ResultPacket());
		processor.processOutput(new SearchTransaction(null, response));
		
		// No processing time
		st.getResponse().getResultPacket().setPadreElapsedTime(null);
		processor.processOutput(st);
	}
	
	@Test
	public void test() throws OutputProcessorException {
		Assert.assertEquals(-1, monitor.getAveragePadreProcessingTime(), 0);
		Assert.assertEquals(0, monitor.getNbQueries());
		
		st.getResponse().getResultPacket().setPadreElapsedTime(123);
		processor.processOutput(st);
		
		Assert.assertEquals(123, monitor.getAveragePadreProcessingTime(), 0.1);
		Assert.assertEquals(1, monitor.getNbQueries());
		
	}
	
}
