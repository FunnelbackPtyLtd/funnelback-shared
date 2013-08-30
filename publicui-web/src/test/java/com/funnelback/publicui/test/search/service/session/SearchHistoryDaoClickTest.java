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
import com.funnelback.publicui.search.service.SearchHistoryRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class SearchHistoryDaoClickTest extends SessionDaoTest {

    @Autowired
    private SearchHistoryRepository repository;
    
    @Override
    public void before() throws Exception {
        for (int i=0; i<5; i++) {
            repository.saveClick(generateRandomClickHistory());
        }
    }
    
    @Test
    public void testGetClickhHistory() {
        Calendar c = Calendar.getInstance();
        for (int i=0; i<3; i++) {
            c.add(Calendar.DAY_OF_MONTH, i);
            ClickHistory ch = new ClickHistory();
            ch.setClickDate(c.getTime());
            ch.setCollection(collection.getId());
            ch.setIndexUrl(URI.create("funnelback://test.result/"+i));
            ch.setSummary("Summary #"+i);
            ch.setTitle("Title #"+i);
            ch.setUserId(user.getId());
    
            repository.saveClick(ch);
        }
        List<ClickHistory> history = repository.getClickHistory(user, collection, 10);
        
        assertEquals(3, history.size());
        ClickHistory previous = null;
        for (int i=2,j=0; j<3; i--,j++) {
            ClickHistory ch = history.get(j);
            assertEquals(user.getId(), ch.getUserId());
            assertEquals(collection.getId(), ch.getCollection());
            assertTrue(ch.getIndexUrl().toString().matches("funnelback://test.result/"+i));
            assertTrue(ch.getSummary().matches("Summary #"+i));
            assertTrue(ch.getTitle().matches("Title #"+i));
            if (previous != null) {
                assertTrue("Should be sorted by descending date", previous.getClickDate().after(ch.getClickDate()));
            }
            
            previous = ch;
        }
        
        repository.clearClickHistory(user, collection);
        
        assertEquals(0, repository.getClickHistory(user, collection, 10).size());
    }
    
    public void testMaxSize() {
        for (int i=0; i<10; i++) {
            ClickHistory ch = new ClickHistory();
            ch.setClickDate(new Date());
            ch.setCollection(collection.getId());
            ch.setIndexUrl(URI.create("funnelback://test.result/"+i));
            ch.setSummary("Summary #"+i);
            ch.setTitle("Title #"+i);
            ch.setUserId(user.getId());
    
            repository.saveClick(ch);
        }
        assertEquals(2, repository.getClickHistory(user, collection, 2).size());
        assertEquals(7, repository.getClickHistory(user, collection, 7).size());
        assertEquals(10, repository.getClickHistory(user, collection, 20).size());
    }
    
    @Test
    public void testClearEmptyHistory() {
        assertEquals(0, repository.getClickHistory(user, collection, 10).size());
        repository.clearClickHistory(user, collection);
        assertEquals(0, repository.getClickHistory(user, collection, 10).size());
    }
    
    @Test
    public void addExistingEntry() {
        Calendar c = Calendar.getInstance();
        ClickHistory ch1 = new ClickHistory();
        ch1.setClickDate(c.getTime());
        ch1.setCollection(collection.getId());
        ch1.setIndexUrl(URI.create("funnelback://test.result/"));
        ch1.setSummary("Summary");
        ch1.setTitle("Title");
        ch1.setUserId(user.getId());

        c.add(Calendar.DAY_OF_MONTH, 1);
        ClickHistory ch2 = new ClickHistory();
        ch2.setClickDate(c.getTime());
        ch2.setCollection(collection.getId());
        ch2.setIndexUrl(URI.create("funnelback://test.result/"));
        ch2.setSummary("Summary updated");
        ch2.setTitle("Title updated");
        ch2.setUserId(user.getId());

        repository.saveClick(ch1);
        repository.saveClick(ch2);

        List<ClickHistory> history = repository.getClickHistory(user, collection, 10);
        assertEquals(1, history.size());
        
        ClickHistory ch = history.get(0);
        assertEquals("The date should have been updated", c.getTime(), ch.getClickDate());
        assertEquals(collection.getId(), ch.getCollection());
        assertEquals(URI.create("funnelback://test.result/"), ch.getIndexUrl());
        assertEquals("Summary", ch.getSummary());
        assertEquals("Title", ch.getTitle());
        assertEquals(user.getId(), ch.getUserId());
    }



}
