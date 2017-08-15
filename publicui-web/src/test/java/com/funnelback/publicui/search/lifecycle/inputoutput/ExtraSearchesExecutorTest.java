package com.funnelback.publicui.search.lifecycle.inputoutput;

import static com.funnelback.common.config.DefaultValues.ModernUI.EXTRA_SEARCH_TIMEOUT_MS;
import static com.funnelback.common.config.DefaultValues.ModernUI.EXTRA_SEARCH_TOTAL_TIMEOUT_MS;
import static com.funnelback.common.config.Keys.ModernUI.EXTRA_SEARCH_TIMEOUT;
import static com.funnelback.common.config.Keys.ModernUI.EXTRA_SEARCH_TOTAL_TIMEOUT;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.FutureTask;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.funnelback.common.config.Config;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;


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
    public void testTimeRecordedForFailedExtraSearch() throws Exception {
        SearchTransaction st = getSearchTransactionWithMockConfig();
        st.getExtraSearchesAproxTimeSpent().set(0);
        setTimeouts(st, 100, 300);
        
        FutureTask<SearchTransaction> future = mock(FutureTask.class);
        when(future.get(anyLong(), any())).thenAnswer(new Answer<SearchTransaction>() {

            @Override
            public SearchTransaction answer(InvocationOnMock invocation) throws Throwable {
                Thread.sleep(3);
                // Simulat the extra search failing.
                throw new java.util.concurrent.TimeoutException();
            }
            
        });
        
        extraSearchExecutor.waitForExtraSearch(st, "name", future);
        
        Assert.assertTrue("Should have taken atleast 3ms as we waited a total of 3ms", 
            st.getExtraSearchesAproxTimeSpent().get() >= 3);
        
        Assert.assertTrue(st.isAnyExtraSearchesIncomplete());
        
        verify(future, times(1)).cancel(true);
    }
    
    
    @Test
    public void testTimeRecordedFromSearchTransaction() throws Exception {
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
        
        Assert.assertEquals(100, st.getExtraSearchesAproxTimeSpent().get());
        
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
    
    private SearchTransaction getSearchTransactionWithMockConfig(){
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), new SearchResponse());
        Collection collection = mock(Collection.class);
        Config config = mock(Config.class);
        when(collection.getConfiguration()).thenReturn(config);
        st.getQuestion().setCollection(collection);
        
        return st;
    }
    
    private void setTimeouts(SearchTransaction st, long singleExtraSearchTimeout, long totalExtraSearchTimeout) {
        when(st.getQuestion().getCollection().getConfiguration()
            .valueAsLong(EXTRA_SEARCH_TIMEOUT, EXTRA_SEARCH_TIMEOUT_MS))
        .thenReturn(singleExtraSearchTimeout);
        
        when(st.getQuestion().getCollection().getConfiguration()
            .valueAsLong(EXTRA_SEARCH_TOTAL_TIMEOUT, EXTRA_SEARCH_TOTAL_TIMEOUT_MS))
        .thenReturn(totalExtraSearchTimeout);
    }
    
    
    
}
