package com.funnelback.publicui.test.search.lifecycle;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.StopWatch;

import com.funnelback.publicui.search.lifecycle.DefaultSearchTransactionProcessor;
import com.funnelback.publicui.search.lifecycle.data.DataFetcher;
import com.funnelback.publicui.search.lifecycle.input.InputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessor;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.test.mock.MockDataFetcher;
import com.funnelback.publicui.test.mock.MockInputProcessor;
import com.funnelback.publicui.test.mock.MockOutputProcessor;

public class DefaultSearchTransactionProcessorTest {

    @Test
    public void test() {
        List<InputProcessor> input = new ArrayList<InputProcessor>();
        input.add(new MockInputProcessor());
        
        List<OutputProcessor> output = new ArrayList<OutputProcessor>();
        output.add(new MockOutputProcessor());
        
        List<DataFetcher> fetcher = new ArrayList<DataFetcher>();
        fetcher.add(new MockDataFetcher());
        
        DefaultSearchTransactionProcessor processor = new DefaultSearchTransactionProcessor();
        processor.setInputFlow(input);
        processor.setOutputFlow(output);
        processor.setDataFetchers(fetcher);
        
        SearchQuestion q = new SearchQuestion();
        q.setCollection(new Collection("a-test", null));
        q.setProfile("a-profile");
        
        SearchTransaction st = processor.process(q, null);
        
        Assert.assertNotNull(st);
        Assert.assertNotNull(st.getResponse());
        
        StopWatch sw = st.getResponse().getPerformanceMetrics();
        // Stop the stopwatch that was started for the output phase
        sw.stop();
        
        Assert.assertNotNull(sw);
        Assert.assertEquals(4, sw.getTaskCount());
        Assert.assertEquals("input:MockInputProcessor", sw.getTaskInfo()[0].getTaskName());
        Assert.assertEquals("datafetch:MockDataFetcher", sw.getTaskInfo()[1].getTaskName());
        Assert.assertEquals("output:MockOutputProcessor", sw.getTaskInfo()[2].getTaskName());
        Assert.assertEquals("output:render", sw.getTaskInfo()[3].getTaskName());
        
        Assert.assertTrue(((MockInputProcessor) input.get(0)).isTraversed());
        Assert.assertTrue(((MockOutputProcessor) output.get(0)).isTraversed());
        Assert.assertTrue(((MockDataFetcher) fetcher.get(0)).isTraversed());
    }
    
}
