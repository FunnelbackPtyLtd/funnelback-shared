package com.funnelback.publicui.test.search.lifecycle.input.processors;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.QueryTimeoutException;

import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.Session;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.session.CartResult;
import com.funnelback.publicui.search.model.transaction.session.ClickHistory;
import com.funnelback.publicui.search.model.transaction.session.SearchHistory;
import com.funnelback.publicui.search.model.transaction.session.SearchSession;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.service.ResultsCartRepository;
import com.funnelback.publicui.search.service.SearchHistoryRepository;
import com.funnelback.publicui.test.mock.MockResultsCartRepository;
import com.funnelback.publicui.test.mock.MockSearchHistoryRepository;

public class SessionTests {

    private Session processor;
    private MockSearchHistoryRepository searchHistoryRepository = new MockSearchHistoryRepository();
    private ResultsCartRepository resultsCartRepository = new MockResultsCartRepository();
    
    private SearchTransaction st;
 
    @Before
    public void before() {
        processor = new Session();
        processor.setSearchHistoryRepository(searchHistoryRepository);
        processor.setResultsCartRepository(resultsCartRepository);
        
        Collection c = new Collection("dummy", new NoOptionsConfig("dummy").setValue(Keys.ModernUI.SESSION, "true"));
        
        SearchQuestion question = new SearchQuestion();
        question.setCollection(c);
        st = new SearchTransaction(question, null);
        st.setSession(new SearchSession(new SearchUser("user")));

    }

    @Test
    public void testMissingData() throws InputProcessorException {
        // No transaction
        processor.processInput(null);
        
        // No question
        processor.processInput(new SearchTransaction(null, null));
        
        // No collection
        SearchQuestion question = new SearchQuestion();
        SearchTransaction st = new SearchTransaction(question, null);
        processor.processInput(st);
        Assert.assertNull(st.getSession());
        
        // No query
        question.setCollection(new Collection("dummy", null));
        processor.processInput(new SearchTransaction(question, null));
        Assert.assertNull(st.getSession());
    }
    
    @Test
    public void testNoSession() throws InputProcessorException {
        st.setSession(null);

        processor.processInput(st);
        
        Assert.assertNull(st.getSession());
    }
    
    @Test
    public void testNoSessionDataStored() throws InputProcessorException {
        processor.processInput(st);
        
        Assert.assertEquals(0, st.getSession().getSearchHistory().size());
        Assert.assertEquals(0, st.getSession().getClickHistory().size());
        Assert.assertEquals(0, st.getSession().getResultsCart().size());
    }
    
    @Test
    public void testSessionData() throws InputProcessorException {
        ClickHistory ch1 = new ClickHistory();
        ch1.setCollection("dummy");
        ch1.setUserId("user");
        searchHistoryRepository.saveClick(ch1);
        ClickHistory ch2 = new ClickHistory();
        ch2.setCollection("other");
        ch2.setUserId("other user");
        searchHistoryRepository.saveClick(ch2);
        
        SearchHistory sh1 = new SearchHistory();
        sh1.setCollection("dummy");
        sh1.setUserId("user");
        searchHistoryRepository.saveSearch(sh1);
        SearchHistory sh2 = new SearchHistory();
        sh2.setCollection("other");
        sh2.setUserId("other user");
        searchHistoryRepository.saveSearch(sh2);
        
        CartResult cr1 = new CartResult();
        cr1.setCollection("dummy");
        cr1.setUserId("user");
        resultsCartRepository.addToCart(cr1);
        CartResult cr2 = new CartResult();
        cr2.setCollection("other");
        cr2.setUserId("other user");
        resultsCartRepository.addToCart(cr2);

        processor.processInput(st);
        
        Assert.assertEquals(1, st.getSession().getSearchHistory().size());
        Assert.assertEquals(sh1, st.getSession().getSearchHistory().get(0));
        Assert.assertEquals(1, st.getSession().getClickHistory().size());
        Assert.assertEquals(ch1, st.getSession().getClickHistory().get(0));
        Assert.assertEquals(1, st.getSession().getResultsCart().size());
        Assert.assertEquals(cr1, st.getSession().getResultsCart().get(0));
    }
    
    @Test
    public void testDataAccessExceptionShouldNotCrash() throws InputProcessorException {
        SearchHistoryRepository r = mock(SearchHistoryRepository.class);
        when(r.getSearchHistory(any(SearchUser.class), any(Collection.class), anyInt()))
            .thenThrow(new QueryTimeoutException(""));
        processor.setSearchHistoryRepository(r);
        processor.processInput(st);
        
        r = mock(SearchHistoryRepository.class);
        when(r.getClickHistory(any(SearchUser.class), any(Collection.class), anyInt()))
            .thenThrow(new QueryTimeoutException(""));
        processor.setSearchHistoryRepository(r);
        processor.processInput(st);

        ResultsCartRepository rc = mock(ResultsCartRepository.class);
        when(rc.getCart(any(SearchUser.class), any(Collection.class)))
            .thenThrow(new QueryTimeoutException(""));
        processor.setResultsCartRepository(rc);
        processor.processInput(st);

        Assert.assertEquals(0, st.getSession().getSearchHistory().size());
        Assert.assertEquals(0, st.getSession().getClickHistory().size());
        Assert.assertEquals(0, st.getSession().getResultsCart().size());
    }
    
    @Test
    public void testDisabled() throws InputProcessorException {
        st.getQuestion().getCollection().getConfiguration()
            .setValue(Keys.ModernUI.SESSION, "false");
        
        SearchHistoryRepository r = mock(SearchHistoryRepository.class);
        when(r.getSearchHistory(any(SearchUser.class), any(Collection.class), anyInt()))
            .thenThrow(new RuntimeException());
        when(r.getClickHistory(any(SearchUser.class), any(Collection.class), anyInt()))
            .thenThrow(new RuntimeException());
        ResultsCartRepository rc = mock(ResultsCartRepository.class);
        when(rc.getCart(any(SearchUser.class), any(Collection.class)))
            .thenThrow(new QueryTimeoutException(""));
        
        processor.setSearchHistoryRepository(r);
        processor.setResultsCartRepository(rc);

        processor.processInput(st);

        Assert.assertEquals(0, st.getSession().getSearchHistory().size());
        Assert.assertEquals(0, st.getSession().getClickHistory().size());
        Assert.assertEquals(0, st.getSession().getResultsCart().size());
    }

}