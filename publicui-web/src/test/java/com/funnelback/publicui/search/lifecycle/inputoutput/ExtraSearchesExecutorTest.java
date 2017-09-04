package com.funnelback.publicui.search.lifecycle.inputoutput;

import static com.funnelback.common.config.DefaultValues.ModernUI.EXTRA_SEARCH_TIMEOUT_MS;
import static com.funnelback.common.config.Keys.ModernUI.EXTRA_SEARCH_TIMEOUT;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.ldap.transaction.compensating.manager.TransactionAwareDirContextInvocationHandler;

import com.funnelback.common.config.Config;
import com.funnelback.config.configtypes.service.ServiceConfigReadOnly;
import com.funnelback.config.keys.Keys.FrontEndKeys;
import com.funnelback.publicui.search.lifecycle.SearchTransactionProcessor;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;


public class ExtraSearchesExecutorTest {
    
    private ExtraSearchesExecutor extraSearchExecutor = new ExtraSearchesExecutor();
    

    @Test
    public void testTimeLeft() {
        SearchTransaction st = getSearchTransactionWithMockConfig();
        st.getExtraSearchesAproxTimeSpent().set(0);
        setTimeouts(st, 100, 300);
        
        Assert.assertEquals("Time to wait should not exceed the max time a single extra search can run in.",
            100, extraSearchExecutor.getTimeToWaitForExtraSearch(st));
        
        setTimeouts(st, 100, 50);
        
        Assert.assertEquals("Time to wait should not exceed the max time all extra searches can run in.",
            50, extraSearchExecutor.getTimeToWaitForExtraSearch(st));
        
        // Simulat extra searches have run for a total of 290ms so far.
        st.getExtraSearchesAproxTimeSpent().set(290);
        setTimeouts(st, 100, 300);
        
        Assert.assertEquals("Time to wait should take into account time already spent in extra searches.",
            10, extraSearchExecutor.getTimeToWaitForExtraSearch(st));
    }
    
    @Test
    public void testFailingExtraSearchIsRecorded() throws Exception {
        SearchTransaction st = getSearchTransactionWithMockConfig();
        st.getExtraSearchesAproxTimeSpent().set(0);
        setTimeouts(st, 100, 300);
        
        FutureTask<SearchTransaction> future = mock(FutureTask.class);
        when(future.get(anyLong(), any())).thenAnswer(new Answer<SearchTransaction>() {

            @Override
            public SearchTransaction answer(InvocationOnMock invocation) throws Throwable {
                throw new java.util.concurrent.TimeoutException();
            }
            
        });
        
        extraSearchExecutor.waitForExtraSearch(st, "name", future);
        
        
        
        Assert.assertTrue(st.isAnyExtraSearchesIncomplete());
        
        verify(future, times(1)).cancel(true);
    }
    
    
    @Test
    public void testNonFailingExtraSearch() throws Exception {
        SearchTransaction st = getSearchTransactionWithMockConfig();
        st.getExtraSearchesAproxTimeSpent().set(0);
        setTimeouts(st, 100, 300);
        
        FutureTask<SearchTransaction> future = mock(FutureTask.class);
        when(future.get(anyLong(), any())).thenAnswer(new Answer<SearchTransaction>() {

            @Override
            public SearchTransaction answer(InvocationOnMock invocation) throws Throwable {
                // In this case the search transaction took longer than it did for us to wait for the 
                // transaction to complete.
                SearchTransaction st = mock(SearchTransaction.class, RETURNS_DEEP_STUBS);
                when(st.getError()).thenReturn(null);
                when(st.getResponse().getPerformanceMetrics().getTotalTimeMillis()).thenReturn(100L);
                return st;
            }
            
        });
        
        extraSearchExecutor.waitForExtraSearch(st, "name", future);
        
        Assert.assertFalse(st.isAnyExtraSearchesIncomplete());
    }
    
    @Test
    public void testTimeRecordedFromSearchTransactionOverTimeWaited() throws Exception {
        SearchTransaction st = getSearchTransactionWithMockConfig();
        st.getExtraSearchesAproxTimeSpent().set(0);
        setTimeouts(st, 100, 300);
        
        FutureTask<SearchTransaction> future = mock(FutureTask.class);
        when(future.get(anyLong(), any())).thenAnswer(new Answer<SearchTransaction>() {

            @Override
            public SearchTransaction answer(InvocationOnMock invocation) throws Throwable {
                // In this case the search transaction took no time but we waited for a few ms
                // in this case we trust the time in the search transaction as the search may
                // not have started.
                SearchTransaction st = mock(SearchTransaction.class, RETURNS_DEEP_STUBS);
                when(st.getError()).thenReturn(null);
                when(st.getResponse().getPerformanceMetrics().getTotalTimeMillis()).thenReturn(0L);
                Thread.sleep(2);
                return st;
            }
            
        });
        
        extraSearchExecutor.waitForExtraSearch(st, "name", future);
        
        Assert.assertEquals(0L, st.getExtraSearchesAproxTimeSpent().get());
        
        Assert.assertFalse(st.isAnyExtraSearchesIncomplete());
    }
    
    @Test
    public void testCallableRecordsTotalTime() throws Exception {
        SearchTransactionProcessor transactionProcessor = new SearchTransactionProcessor() {
            
            @Override
            public SearchTransaction process(SearchQuestion q, SearchUser user) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
                return mock(SearchTransaction.class);
            }
        };
        
        ExtraSearchesExecutor executor = new ExtraSearchesExecutor();
        executor.setTransactionProcessor(transactionProcessor);
        
        SearchTransaction st = getSearchTransactionWithMockConfig();
        
        Callable<SearchTransaction> call = executor.makeCallable(st, "", new SearchQuestion(), null, new HashMap<>());
        
        st.getExtraSearchesAproxTimeSpent().set(0);
        
        Thread.currentThread().interrupt();
        try {
            call.call();
            
            Assert.assertTrue(0 < st.getExtraSearchesAproxTimeSpent().get());
        } finally {
            // Clear thread unterrupt status, so other tests don't fail.
            Thread.currentThread().interrupted();
        }
    }
    
    private SearchTransaction getSearchTransactionWithMockConfig(){
        SearchTransaction st = new SearchTransaction(spy(new SearchQuestion()), new SearchResponse());
        Collection collection = mock(Collection.class);
        Config config = mock(Config.class);
        when(collection.getConfiguration()).thenReturn(config);
        st.getQuestion().setCollection(collection);
        
        ServiceConfigReadOnly serviceConfigReadOnly = mock(ServiceConfigReadOnly.class);
        doReturn(serviceConfigReadOnly).when(st.getQuestion()).getCurrentProfileConfig();
        
        return st;
    }
    
    private void setTimeouts(SearchTransaction st, long singleExtraSearchTimeout, long totalExtraSearchTimeout) {
        when(st.getQuestion().getCollection().getConfiguration()
            .valueAsLong(EXTRA_SEARCH_TIMEOUT, EXTRA_SEARCH_TIMEOUT_MS))
        .thenReturn(singleExtraSearchTimeout);
        
        when(st.getQuestion().getCurrentProfileConfig().get(FrontEndKeys.ModernUI.EXTRA_SEARCH_TOTAL_TIMEOUT))
            .thenReturn(totalExtraSearchTimeout);
        
    }
    
    
    
}
