package com.funnelback.publicui.test.search.lifecycle.output.processors;

import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.Stack;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import com.funnelback.common.config.Collection.Type;
import com.funnelback.common.config.Config;
import com.funnelback.common.function.CallableCE;
import com.funnelback.common.function.CallableNoCE;
import com.funnelback.common.lock.LockWasNotAcquiredException;
import com.funnelback.common.lock.QueryReadLockI;
import com.funnelback.common.padre.index.IndexProcessor;
import com.funnelback.common.padre.index.IndexProcessorWithCE;
import com.funnelback.common.padre.index.ProcessIndex;
import com.funnelback.common.system.EnvironmentVariableException;
import com.funnelback.publicui.search.lifecycle.output.processors.FixMetaCollectionUpdateDate;
import com.funnelback.publicui.search.model.padre.Details;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

import lombok.AllArgsConstructor;

public class FixMetaCollectionUpdateDateTest {
    
    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();
    
    @Test
    public void testDateCheckOnStem() throws IOException, ParseException {
        File indexStem = tempDir.newFile("index");
        File indexTime = tempDir.newFile("index_time");
        
        FileUtils.writeStringToFile(indexTime, "Thu Apr 06 20:52:53 2017");
        
        long result = new FixMetaCollectionUpdateDate().componentUpdateTime(indexStem.getAbsolutePath());
        Assert.assertEquals("Expected correct time to be parsed", result, 1491475973000l);
    }

    @Test
    public void testNonMetaCollection() throws EnvironmentVariableException, FileNotFoundException {
        Stack<Long> components = new Stack<Long>();
        components.addAll(Arrays.asList(new Long[]{9999l, 9999l, 9999l}));
        TestFixMetaCollectionUpdateDateOutputProcessor processor = new TestFixMetaCollectionUpdateDateOutputProcessor(components);
        processor.setProcessIndex(new TestIndexProcessor());
        processor.setQueryReadLock(new TestQueryReadLock());
        
        Config config = Mockito.mock(Config.class);
        Mockito.when(config.getCollectionType()).thenReturn(Type.web);
        
        SearchQuestion question = mock(SearchQuestion.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(question.getCollection().getConfiguration()).thenReturn(config);
        
        ResultPacket resultPacket = new ResultPacket();
        
        Details details = new Details();
        
        details.setCollectionUpdated(new Date(1234l));
        
        resultPacket.setDetails(details);
        
        SearchResponse response = new SearchResponse();
        response.setResultPacket(resultPacket);
        
        SearchTransaction st = new SearchTransaction(question, response);
        
        processor.processOutput(st);
        
        Assert.assertEquals("Expected updated date to be untouched, because this is not a meta collection", st.getResponse().getResultPacket().getDetails().getCollectionUpdated().getTime(), 1234l);
    }

    @Test
    public void testFindMetaDate() throws EnvironmentVariableException, FileNotFoundException {
        Stack<Long> components = new Stack<Long>();
        components.addAll(Arrays.asList(new Long[]{1l, 3l, 2l}));
        TestFixMetaCollectionUpdateDateOutputProcessor processor = new TestFixMetaCollectionUpdateDateOutputProcessor(components);
        processor.setProcessIndex(new TestIndexProcessor());
        processor.setQueryReadLock(new TestQueryReadLock());
        
        Config config = Mockito.mock(Config.class);
        Mockito.when(config.getCollectionType()).thenReturn(Type.meta);
        
        SearchQuestion question = mock(SearchQuestion.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(question.getCollection().getConfiguration()).thenReturn(config);
        
        ResultPacket resultPacket = new ResultPacket();
        
        Details details = new Details();
        
        details.setCollectionUpdated(new Date(0l));
        
        resultPacket.setDetails(details);
        
        SearchResponse response = new SearchResponse();
        response.setResultPacket(resultPacket);
        
        SearchTransaction st = new SearchTransaction(question, response);
        
        processor.processOutput(st);
        
        Assert.assertEquals("Expected updated date to be the greatest of any component", st.getResponse().getResultPacket().getDetails().getCollectionUpdated().getTime(), 3l);
    }

    class TestIndexProcessor extends ProcessIndex {
        // Always fakes three meta components for simplicity
        public <X extends Exception, Y extends Exception> void processIndex(String collection, String indexStem, 
            IndexProcessorWithCE<X, Y> indexProcessor) 
                throws IndexProcessor.MissingIndexException, X, Y {
            indexProcessor.atComponentIndex("1", "1-stem");
            indexProcessor.atComponentIndex("2", "2-stem");
            indexProcessor.atComponentIndex("3", "3-stem");
        }
    }
    
    class TestQueryReadLock implements QueryReadLockI {

        @Override
        public <R> R doWithQueryReadLock(Config config, CallableNoCE<R> task) throws LockWasNotAcquiredException {
            return task.call();
        }

        @Override
        public <R, X extends Exception> R doWithReadLockWithCE(Config config, CallableCE<R, X> task) throws X, LockWasNotAcquiredException {
            return task.call();
        }
        
    }
    
    @AllArgsConstructor
    class TestFixMetaCollectionUpdateDateOutputProcessor extends FixMetaCollectionUpdateDate {
        private final Stack<Long> desiredResults;
        
        @Override
        public long componentUpdateTime(String indexStem) {
            long result = desiredResults.pop();
            return result;
        }
    }
}
