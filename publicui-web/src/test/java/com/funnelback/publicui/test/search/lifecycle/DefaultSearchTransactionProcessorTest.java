package com.funnelback.publicui.test.search.lifecycle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.util.StopWatch;

import com.funnelback.publicui.search.lifecycle.DefaultSearchTransactionProcessor;
import com.funnelback.publicui.search.lifecycle.data.DataFetchException;
import com.funnelback.publicui.search.lifecycle.data.DataFetcher;
import com.funnelback.publicui.search.lifecycle.input.InputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchError.Reason;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.test.mock.MockDataFetcher;
import com.funnelback.publicui.test.mock.MockInputProcessor;
import com.funnelback.publicui.test.mock.MockOutputProcessor;

public class DefaultSearchTransactionProcessorTest {

    private SearchQuestion q;
    private DefaultSearchTransactionProcessor processor;
    private List<InputProcessor> input;
    private List<DataFetcher> data;
    private List<OutputProcessor> output;
    
    @Before
    public void before() {
        input = new ArrayList<>();
        data = new ArrayList<>();
        output = new ArrayList<>();
        
        processor = new DefaultSearchTransactionProcessor();
        processor.setInputFlow(input);
        processor.setOutputFlow(output);
        processor.setDataFetchers(data);
        
        q = new SearchQuestion();
        q.setCollection(new Collection("a-test", null));
        q.setProfile("a-profile");

    }
    
    @Test
    public void test() {
        input.add(new MockInputProcessor());
        output.add(new MockOutputProcessor());
        data.add(new MockDataFetcher());
        
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
        Assert.assertTrue(((MockDataFetcher) data.get(0)).isTraversed());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testErrorHandling() throws Exception {
        Map<Class<? extends Exception>, Reason> errorReasons = new HashMap<>();
        errorReasons.put(InputProcessorException.class, Reason.InputProcessorError);
        errorReasons.put(OutputProcessorException.class, Reason.OutputProcessorError);
        errorReasons.put(DataFetchException.class, Reason.DataFetchError);
        errorReasons.put(ObscureException.class, Reason.Unknown);
        
        for (Class<Exception> clazz: new Class[] {
                        InputProcessorException.class,
                        OutputProcessorException.class,
                        DataFetchException.class
        }) {
            InputProcessor inputProcessor = Mockito.mock(InputProcessor.class);
            Mockito.doThrow(clazz).when(inputProcessor).processInput(Mockito.any());
            
            input.clear();
            input.add(inputProcessor);
            
            SearchTransaction st = processor.process(q, null);
            
            Assert.assertNotNull(clazz.getSimpleName() + " error should have been handled", st.getError());
            Assert.assertEquals("Invalid reason given for " + clazz.getSimpleName(), errorReasons.get(clazz), st.getError().getReason());
            Assert.assertEquals("Invalid error additional data stored", clazz, st.getError().getAdditionalData().getClass());
            
        }
    }
    
    private static class ObscureException extends RuntimeException {

        private static final long serialVersionUID = 1L;
    }
    
}
