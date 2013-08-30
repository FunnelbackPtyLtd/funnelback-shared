package com.funnelback.publicui.test.search.service.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.publicui.search.model.transaction.session.ClickHistory;
import com.funnelback.publicui.search.model.transaction.session.SearchHistory;
import com.funnelback.publicui.search.service.SearchHistoryRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class SearchHistoryDaoSearchTest extends SessionDaoTest {

    @Autowired
    private SearchHistoryRepository repository;
    
    @Override
    public void before() throws Exception {
        for (int i=0; i<5; i++) {
            repository.saveSearch(generateRandomSearchHistory());
        }
    }
    
    @Test
    public void testGetSearchhHistory() {
        Calendar c = Calendar.getInstance();
        for (int i=0; i<3; i++) {
            c.add(Calendar.DAY_OF_MONTH, i);
            SearchHistory sh = new SearchHistory();
            sh.setCollection(collection.getId());
            sh.setCurrStart(i);
            sh.setNumRanks(i*10);
            sh.setOriginalQuery("a query #"+i);
            sh.setQueryAsProcessed("a processed query #"+i);
            sh.setSearchDate(c.getTime());
            sh.setSearchParams("&a+param=a+value&another%20one=a+different%20value&i="+i);
            sh.setTotalMatching(i*100);
            sh.setUserId(user.getId());
    
            repository.saveSearch(sh);
        }
        List<SearchHistory> history = repository.getSearchHistory(user, collection, 10);
        
        assertEquals(3, history.size());
        SearchHistory previous = null;
        for (int i=2,j=0; j<3; i--,j++) {
            SearchHistory sh = history.get(j);
            assertEquals(collection.getId(), sh.getCollection());
            assertEquals(i, sh.getCurrStart());
            assertEquals(i*10, sh.getNumRanks());
            assertEquals("a query #"+i, sh.getOriginalQuery());
            assertEquals("a processed query #"+i, sh.getQueryAsProcessed());
            assertEquals("&a+param=a+value&another%20one=a+different%20value&i="+i, sh.getSearchParams());
            assertEquals(i*100, sh.getTotalMatching());
            assertEquals(user.getId(), sh.getUserId());
            
            if (previous != null) {
                assertTrue("Should be sorted by descending date", previous.getSearchDate().after(sh.getSearchDate()));
            }
            
            previous = sh;
        }
        
        repository.clearSearchHistory(user, collection);
        
        assertEquals(0, repository.getSearchHistory(user, collection, 10).size());
    }
    
    public void testMaxSize() {
        for (int i=0; i<10; i++) {
            SearchHistory sh = new SearchHistory();
            sh.setCollection(collection.getId());
            sh.setCurrStart(i);
            sh.setNumRanks(i*10);
            sh.setOriginalQuery("a query #"+i);
            sh.setQueryAsProcessed("a processed query #"+i);
            sh.setSearchDate(new Date());
            sh.setSearchParams("&a+param=a+value&another%20one=a+different%20value&i="+i);
            sh.setTotalMatching(i*100);
            sh.setUserId(user.getId());
    
            repository.saveSearch(sh);
        }
        assertEquals(2, repository.getSearchHistory(user, collection, 2).size());
        assertEquals(7, repository.getSearchHistory(user, collection, 7).size());
        assertEquals(10, repository.getSearchHistory(user, collection, 20).size());
    }
    
    @Test
    public void testClearEmptyHistory() {
        assertEquals(0, repository.getSearchHistory(user, collection, 10).size());
        repository.clearSearchHistory(user, collection);
        assertEquals(0, repository.getSearchHistory(user, collection, 10).size());
    }
    
    @Test
    public void addExistingEntry() {
        Date d1 = new Date();
        SearchHistory sh1 = new SearchHistory();
        sh1.setCollection(collection.getId());
        sh1.setCurrStart(1);
        sh1.setNumRanks(2);
        sh1.setOriginalQuery("a query");
        sh1.setQueryAsProcessed("a processed query");
        sh1.setSearchDate(d1);
        sh1.setSearchParams("&a+param=a+value&another%20one=a+different%20value");
        sh1.setTotalMatching(3);
        sh1.setUserId(user.getId());

        Date d2 = new Date();
        SearchHistory sh2 = new SearchHistory();
        sh2.setCollection(collection.getId());
        sh2.setCurrStart(1);
        sh2.setNumRanks(2);
        sh2.setOriginalQuery("a query");
        sh2.setQueryAsProcessed("a processed query");
        sh2.setSearchDate(d2);
        sh2.setSearchParams("&a+param=a+value&another%20one=a+different%20value");
        sh2.setTotalMatching(3);
        sh2.setUserId(user.getId());

        repository.saveSearch(sh1);
        repository.saveSearch(sh2);

        List<SearchHistory> history = repository.getSearchHistory(user, collection, 10);
        assertEquals(1, history.size());
        
        SearchHistory sh = history.get(0);
        assertEquals("The date should have been updated", d2, sh.getSearchDate());
        assertEquals(collection.getId(), sh.getCollection());
        assertEquals(1, sh.getCurrStart());
        assertEquals(2, sh.getNumRanks());
        assertEquals("a query", sh.getOriginalQuery());
        assertEquals("a processed query", sh.getQueryAsProcessed());
        assertEquals("&a+param=a+value&another%20one=a+different%20value", sh.getSearchParams());
        assertEquals(3, sh.getTotalMatching());
        assertEquals(user.getId(), sh.getUserId());
    }



}
