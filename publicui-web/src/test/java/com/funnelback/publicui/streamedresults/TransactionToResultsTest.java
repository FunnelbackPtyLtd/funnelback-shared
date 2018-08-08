package com.funnelback.publicui.streamedresults;


import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.jxpath.CompiledExpression;
import org.mockito.InOrder;
import org.mockito.Matchers;

import com.funnelback.common.Reference;
import com.funnelback.common.function.BiConsumerWithCE;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.padre.ResultsSummary;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.streamedresults.datafetcher.XJPathResultDataFetcher;
import com.funnelback.publicui.streamedresults.outputstreams.CloseIgnoringOutputStream;

import junit.framework.Assert;
import lombok.AllArgsConstructor;
import lombok.Delegate;
public class TransactionToResultsTest {
    
    @AllArgsConstructor
    public static class DelegateServletOutputStream extends ServletOutputStream {
        // IDK why this needs to be a BOS rather than just a OS
        @Delegate private ByteArrayOutputStream outputStream;

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {}
    }
    
    private final CompiledExpression COMPILEDEXPRESSION = mock(CompiledExpression.class);
    
    private TransactionToResults getATransactionToResults() {
        return new TransactionToResults(mock(DataConverter.class), 
            asList(COMPILEDEXPRESSION), 
            asList("fieldname"), 
            mock(HttpServletResponse.class), 
            mock(XJPathResultDataFetcher.class));
    }
    
    @Test
    public void testOnFirstTransaction() throws Exception {
        TransactionToResults transactionToResults = getATransactionToResults();
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        
        
        when(transactionToResults.getResponse().getOutputStream()).thenReturn(new DelegateServletOutputStream(bos));
        when(transactionToResults.getDataConverter().getContentType()).thenReturn("content type is foo");
        when(transactionToResults.getDataConverter().createWriter(Matchers.isA(CloseIgnoringOutputStream.class)))
            .thenReturn("the writer");
        
        SearchTransaction transaction = mock(SearchTransaction.class, RETURNS_DEEP_STUBS);
        when(transaction.getResponse().getResultPacket().getResultsSummary().getTotalMatching()).thenReturn(100);
        when(transaction.getResponse().getResultPacket().getResultsSummary().getEstimatedCounts()).thenReturn(false);
        
        transactionToResults.onFirstTransaction(transaction);
        
        verify(transactionToResults.getResponse()).setHeader("X-Funnelback-Total-Matching", "100");
        verify(transactionToResults.getResponse()).setHeader("X-Funnelback-Total-Matching-Is-Estimated-Counts", "false");
        verify(transactionToResults.getResponse()).setContentType("content type is foo");
        
        verify(transactionToResults.getDataConverter()).writeHead(eq(asList("fieldname")), eq("the writer"));
    }
    
    @Test
    public void nullTestsForAddHeadersFromFirstTransaction() {
        TransactionToResults transactionToResults = getATransactionToResults();
        
        Consumer<SearchTransaction> check = (transaction) -> {
            transactionToResults.addHeadersFromFirstTransaction(transaction);
            verify(transactionToResults.getResponse(), never()).addHeader(anyString(), anyString());
        };
        
        // Slow build up the search transaction checking that we don't throw a NPE.
        
        SearchTransaction transaction = null;
        check.accept(transaction);
        
        transaction = mock(SearchTransaction.class);
        check.accept(transaction);
        
        when(transaction.getResponse()).thenReturn(mock(SearchResponse.class));
        check.accept(transaction);
        
        when(transaction.getResponse().getResultPacket()).thenReturn(mock(ResultPacket.class));
        check.accept(transaction);
        
        when(transaction.getResponse().getResultPacket().getResultsSummary()).thenReturn(mock(ResultsSummary.class));
        check.accept(transaction);
    }
    
    @Test
    public void testOnEachTransaction() throws Exception {
        AtomicBoolean returnBoolean = new AtomicBoolean(true);
        TransactionToResults nonSpyed = new TransactionToResults(mock(DataConverter.class), 
            asList(COMPILEDEXPRESSION), 
            asList("fieldname"), 
            mock(HttpServletResponse.class), 
            mock(XJPathResultDataFetcher.class)) {
            @Override
            void onFirstTransaction(SearchTransaction transaction) {
                // Do nothing
            }
            @Override
            <T> boolean writeResults(List<Result> results,
                T writer,
                boolean isFirst) {
                
                return returnBoolean.get();
            }
        };
        
        TransactionToResults transactionToResults = spy(nonSpyed);
        
        SearchTransaction transaction = mock(SearchTransaction.class, RETURNS_DEEP_STUBS);
        
        BiConsumerWithCE<Boolean, Integer, IOException> checkWriteResultsCorrectlyCalled = (expectedIsFirst, count) -> {
            verify(transactionToResults, times(count)).writeResults(
                same(transaction.getResponse().getResultPacket().getResults()),
                same(nonSpyed.getWriter()), 
                Matchers.eq(expectedIsFirst)
                );
        };
        
        transactionToResults.onEachTransaction(new Reference<>(transaction));
        
        verify(transactionToResults).onFirstTransaction(transaction);
        checkWriteResultsCorrectlyCalled.accept(true, 1);
        
        // Update the writeResult method so that when it runs it tells the TransactionToResults
        // that it has processed as result
        returnBoolean.set(false);
        
        transactionToResults.onEachTransaction(new Reference<>(transaction));
        checkWriteResultsCorrectlyCalled.accept(true, 2); // We process the first Result in this call so it should be true.
        
        transactionToResults.onEachTransaction(new Reference<>(transaction));
        checkWriteResultsCorrectlyCalled.accept(false, 1); // This time we expect false as we we have processed a Result before.
        
        // We expect that the onFirstTransaction runs exactly once.
        verify(transactionToResults, times(1)).onFirstTransaction(transaction);
    }
    
    @Test
    public void testClose() throws IOException {
        TransactionToResults transactionToResults = getATransactionToResults();
        transactionToResults.close();
        
        InOrder order = inOrder(transactionToResults.getDataConverter());
        // Check that the footer is wrtten before we tell the data converter we are finished with it.
        order.verify(transactionToResults.getDataConverter()).writeFooter(any());
        order.verify(transactionToResults.getDataConverter()).finished(any());
    }

    @Test
    public void testWriteResults() throws IOException {
        TransactionToResults transactionToResults = getATransactionToResults();
        
        
        List<Result> results = asList(mock(Result.class), mock(Result.class));
        
        
        List<List<Object>> values = asList(asList("val1"), asList("val2"));
        
        when(transactionToResults.getResultDataFetcher().fetchFieldValues(transactionToResults.getXpaths(), results.get(0)))
            .thenReturn(values.get(0));
        
        when(transactionToResults.getResultDataFetcher().fetchFieldValues(transactionToResults.getXpaths(), results.get(1)))
        .thenReturn(values.get(1));
        
        
        
        Assert.assertFalse("Return false as isFirst should no be set to false.", 
            transactionToResults.writeResults(results, "writer", true));
        
        DataConverter dataConverter = transactionToResults.getDataConverter();
        
        InOrder orderCheck = inOrder(dataConverter);
        orderCheck.verify(dataConverter, times(1)).writeRecord(transactionToResults.getFieldNames(), values.get(0), "writer");
        orderCheck.verify(dataConverter, times(1)).writeSeperator("writer");
        orderCheck.verify(dataConverter, times(1)).writeRecord(transactionToResults.getFieldNames(), values.get(1), "writer");
        
    }
    
    @Test
    public void testWriteResultsSecondCall() throws IOException {
        TransactionToResults transactionToResults = getATransactionToResults();
        
        List<Result> results = asList(mock(Result.class), mock(Result.class));
        
        
        List<List<Object>> values = asList(asList("val1"), asList("val2"));
        
        when(transactionToResults.getResultDataFetcher().fetchFieldValues(transactionToResults.getXpaths(), results.get(0)))
            .thenReturn(values.get(0));
        
        when(transactionToResults.getResultDataFetcher().fetchFieldValues(transactionToResults.getXpaths(), results.get(1)))
        .thenReturn(values.get(1));
        
        
        //This time we pretend this is the not the first time we write a result
        Assert.assertFalse("Return false as isFirst should no be set to false.", 
            transactionToResults.writeResults(results, "writer", false));
        
        DataConverter dataConverter = transactionToResults.getDataConverter();
        
        InOrder orderCheck = inOrder(dataConverter);
        orderCheck.verify(dataConverter, times(1)).writeSeperator("writer");
        orderCheck.verify(dataConverter, times(1)).writeRecord(transactionToResults.getFieldNames(), values.get(0), "writer");
        orderCheck.verify(dataConverter, times(1)).writeSeperator("writer");
        orderCheck.verify(dataConverter, times(1)).writeRecord(transactionToResults.getFieldNames(), values.get(1), "writer");
    }
    
    @Test
    public void testWriteResultsEmptyList() throws IOException {
        TransactionToResults transactionToResults = getATransactionToResults();
        
        List<Result> results = asList();
        
        //This time we pretend this is the not the first time we write a result
        Assert.assertTrue("Return true as no result was written so the next write is still considered our first.", 
            transactionToResults.writeResults(results, "writer", true));
        
        DataConverter dataConverter = transactionToResults.getDataConverter();
        
        verify(dataConverter, never()).writeSeperator("writer");
        verify(dataConverter, never()).writeRecord(any(), any(), any());
    }

}
