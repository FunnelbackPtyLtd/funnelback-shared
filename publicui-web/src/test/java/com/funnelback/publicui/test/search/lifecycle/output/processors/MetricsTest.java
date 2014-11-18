package com.funnelback.publicui.test.search.lifecycle.output.processors;

import static com.funnelback.publicui.utils.web.MetricsConfiguration.ALL_NS;
import static com.funnelback.publicui.utils.web.MetricsConfiguration.COLLECTION_NS;
import static com.funnelback.publicui.utils.web.MetricsConfiguration.ERRORS_COUNT;
import static com.funnelback.publicui.utils.web.MetricsConfiguration.PADRE_ELAPSED_TIME;
import static com.funnelback.publicui.utils.web.MetricsConfiguration.QUERIES;
import static com.funnelback.publicui.utils.web.MetricsConfiguration.TOTAL_MATCHING;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.codahale.metrics.MetricRegistry;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.lifecycle.output.processors.Metrics;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.padre.ResultsSummary;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class MetricsTest {

    private SearchTransaction st;
    private Metrics processor;
    private MetricRegistry metrics;
    
    @Before
    public void before() {
        metrics = new MetricRegistry();
        processor = new Metrics();
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
        st.getResponse().getResultPacket().setPadreElapsedTime(123);
        st.getResponse().getResultPacket().setResultsSummary(new ResultsSummary());
        st.getResponse().getResultPacket().getResultsSummary().setTotalMatching(456); 
        processor.processOutput(st);
        
        Assert.assertEquals(0, metrics.counter(MetricRegistry.name(ALL_NS, ALL_NS, ERRORS_COUNT)).getCount());
        Assert.assertEquals(456, metrics.histogram(MetricRegistry.name(ALL_NS, TOTAL_MATCHING)).getSnapshot().getMean(), 0.1);
        Assert.assertEquals(123, metrics.histogram(MetricRegistry.name(ALL_NS, PADRE_ELAPSED_TIME)).getSnapshot().getMean(), 0.1);
        Assert.assertEquals(1, metrics.meter(MetricRegistry.name(ALL_NS, QUERIES)).getCount());
        Assert.assertNotSame(0, metrics.meter(MetricRegistry.name(ALL_NS, QUERIES)).getMeanRate());
        
        Assert.assertEquals(0, metrics.counter(MetricRegistry.name(COLLECTION_NS, "metrics._default", ERRORS_COUNT)).getCount());
        Assert.assertEquals(456, metrics.histogram(MetricRegistry.name(COLLECTION_NS, "metrics._default", TOTAL_MATCHING)).getSnapshot().getMean(), 0.1);
        Assert.assertEquals(123, metrics.histogram(MetricRegistry.name(COLLECTION_NS, "metrics._default", PADRE_ELAPSED_TIME)).getSnapshot().getMean(), 0.1);
        Assert.assertEquals(1, metrics.meter(MetricRegistry.name(COLLECTION_NS, "metrics._default", QUERIES)).getCount());
        Assert.assertNotSame(0, metrics.meter(MetricRegistry.name(COLLECTION_NS, "metrics._default", QUERIES)).getMeanRate());
    }
    
}
