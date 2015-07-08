package com.funnelback.publicui.test.search.web.controllers.session;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.publicui.search.service.SearchHistoryRepository;
import com.funnelback.publicui.search.web.controllers.session.SearchHistoryController;
import com.funnelback.publicui.test.mock.MockConfigRepository;
import com.funnelback.publicui.test.search.service.session.SessionDaoTest;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class SearchHistoryControllerTests extends SessionDaoTest {

    @Autowired
    private SearchHistoryController controller;
    
    @Autowired
    private MockConfigRepository configRepository;
    
    @Autowired
    private SearchHistoryRepository repository;

    @Override
    public void before() throws Exception {
        configRepository.addCollection(collection);
    }
    
    @Test
    public void testInvalidCollection() throws IOException {
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.searchHistoryClear(null, user, response);
        assertEquals(404, response.getStatus());

        response = new MockHttpServletResponse();
        controller.clickHistoryClear(null, user, response);
        assertEquals(404, response.getStatus());
    }
    
    @Test
    public void testNoUser() throws IOException {
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.searchHistoryClear(collection, null, response);
        assertEquals(404, response.getStatus());
        
        response = new MockHttpServletResponse();
        controller.clickHistoryClear(collection, null, response);
        assertEquals(404, response.getStatus());
    }
    
    @Test
    public void testEmptyHistory() throws IOException {
        assertEquals(0, repository.getSearchHistory(user, collection, 10).size());
        assertEquals(0, repository.getClickHistory(user, collection, 10).size());

        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.searchHistoryClear(collection, user, response);
        assertEquals(0, repository.getSearchHistory(user, collection, 10).size());
        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertEquals("{\"status\":\"ok\"}", response.getContentAsString());

        response = new MockHttpServletResponse();
        controller.clickHistoryClear(collection, user, response);
        assertEquals(0, repository.getSearchHistory(user, collection, 10).size());
        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertEquals("{\"status\":\"ok\"}", response.getContentAsString());
    }
    
    @Test
    public void testClearSearchHistory() throws IOException {
        for (int i=0; i<3; i++) {
            repository.saveSearch(generateRandomSearchHistory(collection.getId(), user.getId()));
        }
        assertEquals(3, repository.getSearchHistory(user, collection, 10).size());
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.searchHistoryClear(collection, user, response);
        
        assertEquals(0, repository.getSearchHistory(user, collection, 10).size());
        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertEquals("{\"status\":\"ok\"}", response.getContentAsString());
        
        assertEquals(0, repository.getSearchHistory(user, collection, 10).size());
    }

    @Test
    public void testClearClickHistory() throws IOException {
        for (int i=0; i<3; i++) {
            repository.saveClick(generateRandomClickHistory(collection.getId(), user.getId()));
        }
        assertEquals(3, repository.getClickHistory(user, collection, 10).size());
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.clickHistoryClear(collection, user, response);
        
        assertEquals(0, repository.getClickHistory(user, collection, 10).size());
        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertEquals("{\"status\":\"ok\"}", response.getContentAsString());
        
        assertEquals(0, repository.getClickHistory(user, collection, 10).size());
    }

}
