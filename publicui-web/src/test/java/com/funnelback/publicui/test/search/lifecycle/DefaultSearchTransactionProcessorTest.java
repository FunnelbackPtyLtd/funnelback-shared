package com.funnelback.publicui.test.search.lifecycle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.StopWatch;

import com.funnelback.publicui.i18n.I18n;
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
import com.funnelback.publicui.search.model.transaction.SearchQuestion.SearchQuestionType;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.test.mock.MockDataFetcher;
import com.funnelback.publicui.test.mock.MockInputProcessor;
import com.funnelback.publicui.test.mock.MockOutputProcessor;

import lombok.AllArgsConstructor;
import lombok.Data;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class DefaultSearchTransactionProcessorTest {

    private SearchQuestion q;
    private DefaultSearchTransactionProcessor processor;
    private List<InputProcessor> input;
    private List<DataFetcher> data;
    private List<OutputProcessor> output;
    
    @Autowired
    private I18n i18n;
    
    @Before
    public void before() {
        input = new ArrayList<>();
        data = new ArrayList<>();
        output = new ArrayList<>();
        
        processor = new DefaultSearchTransactionProcessor();
        processor.setInputFlow(input);
        processor.setOutputFlow(output);
        processor.setDataFetchers(data);
        processor.setI18n(i18n);
        
        
        q = new SearchQuestion();
        q.setCollection(new Collection("a-test", null));
        q.setProfile("a-profile");
        
        System.out.println(i18n.tr("outputprocessor.padrereturncode.log.failed"));
        System.out.println("DONE");
        

    }
    
    @Test
    public void test() {
        input.add(new MockInputProcessor());
        output.add(new MockOutputProcessor());
        data.add(new MockDataFetcher());
        
        SearchTransaction st = processor.process(q, null, Optional.empty());
        
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
    public void testErrorHandlingInputProcessor() throws Exception {
        InputProcessor inputProcessor = Mockito.mock(InputProcessor.class);
        Mockito.doThrow(InputProcessorException.class).when(inputProcessor).processInput(Mockito.any());
        input.clear();
        input.add(inputProcessor);
        
        SearchTransaction st = processor.process(q, null, Optional.empty());

        this.testError(InputProcessorException.class, st);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testErrorHandlingDataFetcher() throws Exception {
        DataFetcher dataFetcher = Mockito.mock(DataFetcher.class);
        Mockito.doThrow(DataFetchException.class).when(dataFetcher).fetchData(Mockito.any());
        data.clear();
        data.add(dataFetcher);
        
        SearchTransaction st = processor.process(q, null, Optional.empty());

        this.testError(DataFetchException.class, st);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testErrorHandlingOutputProcessor() throws Exception {
        OutputProcessor outputProcessor = Mockito.mock(OutputProcessor.class);
        Mockito.doThrow(OutputProcessorException.class).when(outputProcessor).processOutput(Mockito.any());
        output.clear();
        output.add(outputProcessor);
        
        SearchTransaction st = processor.process(q, null, Optional.empty());

        this.testError(OutputProcessorException.class, st);
    }
    
    private void testError(Class<? extends Exception> exceptionClass, SearchTransaction st) {
        Map<Class<? extends Exception>, Reason> errorReasons = new HashMap<>();
        errorReasons.put(InputProcessorException.class, Reason.InputProcessorError);
        errorReasons.put(OutputProcessorException.class, Reason.OutputProcessorError);
        errorReasons.put(DataFetchException.class, Reason.DataFetchError);
        errorReasons.put(ObscureException.class, Reason.Unknown);
        
        Assert.assertNotNull(exceptionClass.getSimpleName() + " error should have been handled", st.getError());
        Assert.assertEquals("Invalid reason given for " + exceptionClass.getSimpleName(), errorReasons.get(exceptionClass), st.getError().getReason());
        Assert.assertEquals("Invalid error additional data stored", exceptionClass, st.getError().getAdditionalData().getClass());
    }
    
    @Data 
    @AllArgsConstructor
    public static class ProcessorAndException {
        private Class<? extends Exception> exceptionClass;
        private Class<?> processorClass;
    }
    
    @Test
    public void testRecordException() {
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), new SearchResponse());
        // test non extra search
        this.processor.recordException(st, new Exception(""), Reason.DataFetchError, Optional.empty());
        
        // Test extra search case
        this.processor.recordException(st, new Exception(""), Reason.DataFetchError, Optional.of("foo"));
        
        // Test faceted nav extra search case
        st.getQuestion().setQuestionType(SearchQuestionType.FACETED_NAVIGATION_EXTRA_SEARCH);
        this.processor.recordException(st, new Exception(""), Reason.DataFetchError, Optional.of("foo"));
    }
    
    private static class ObscureException extends RuntimeException {

        private static final long serialVersionUID = 1L;
    }
    
}
