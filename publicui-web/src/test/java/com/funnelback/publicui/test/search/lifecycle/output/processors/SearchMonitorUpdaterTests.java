package com.funnelback.publicui.test.search.lifecycle.output.processors;

import static com.funnelback.publicui.utils.web.MetricsConfiguration.ALL_NS;
import static com.funnelback.publicui.utils.web.MetricsConfiguration.COLLECTION_NS;
import static com.funnelback.publicui.utils.web.MetricsConfiguration.ERRORS_COUNT;
import static com.funnelback.publicui.utils.web.MetricsConfiguration.PADRE_ELAPSED_TIME;
import static com.funnelback.publicui.utils.web.MetricsConfiguration.QUERIES;
import static com.funnelback.publicui.utils.web.MetricsConfiguration.TOTAL_MATCHING;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.publicui.search.jmx.SearchMonitor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.lifecycle.output.processors.SearchMonitorUpdater;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.padre.ResultsSummary;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;

public class SearchMonitorUpdaterTests {

	private SearchTransaction st;
	private SearchMonitor monitor;
	private SearchMonitorUpdater processor;
	private MetricsRegistry metrics;
	
	@Before
	public void before() {
		monitor = new SearchMonitor();
		metrics = new MetricsRegistry();
		processor = new SearchMonitorUpdater();
		processor.setMonitor(monitor);
		processor.setMetrics(metrics);
		processor.postConstruct();
		
		st = new SearchTransaction(new SearchQuestion(), new SearchResponse());
		st.getQuestion().setCollection(new Collection("metrics", null));
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
		st.getResponse().getResultPacket().setResultsSummary(new ResultsSummary());
		st.getResponse().getResultPacket().getResultsSummary().setTotalMatching(456); 
		processor.processOutput(st);
		
		Assert.assertEquals(123, monitor.getAveragePadreProcessingTime(), 0.1);
		Assert.assertEquals(1, monitor.getNbQueries());
		
		Assert.assertEquals(0, metrics.newCounter(new MetricName(ALL_NS, ALL_NS, ERRORS_COUNT)).count());
		Assert.assertEquals(456, metrics.newHistogram(new MetricName(ALL_NS, ALL_NS, TOTAL_MATCHING), false).mean(), 0.1);
		Assert.assertEquals(123, metrics.newHistogram(new MetricName(ALL_NS, ALL_NS, PADRE_ELAPSED_TIME), false).mean(), 0.1);
		Assert.assertEquals(1, metrics.newMeter(new MetricName(ALL_NS, ALL_NS, QUERIES), QUERIES, TimeUnit.SECONDS).count());
		Assert.assertNotSame(0, metrics.newMeter(new MetricName(ALL_NS, ALL_NS, QUERIES), QUERIES, TimeUnit.SECONDS).meanRate());
		
		Assert.assertEquals(0, metrics.newCounter(new MetricName(COLLECTION_NS, "metrics._default", ERRORS_COUNT)).count());
		Assert.assertEquals(456, metrics.newHistogram(new MetricName(COLLECTION_NS, "metrics._default", TOTAL_MATCHING), false).mean(), 0.1);
		Assert.assertEquals(123, metrics.newHistogram(new MetricName(COLLECTION_NS, "metrics._default", PADRE_ELAPSED_TIME), false).mean(), 0.1);
		Assert.assertEquals(1, metrics.newMeter(new MetricName(COLLECTION_NS, "metrics._default", QUERIES), QUERIES, TimeUnit.SECONDS).count());
		Assert.assertNotSame(0, metrics.newMeter(new MetricName(COLLECTION_NS, "metrics._default", QUERIES), QUERIES, TimeUnit.SECONDS).meanRate());
	}
	
}
