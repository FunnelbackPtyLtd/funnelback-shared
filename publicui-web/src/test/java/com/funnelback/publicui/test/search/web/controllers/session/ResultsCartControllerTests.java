package com.funnelback.publicui.test.search.web.controllers.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.eq;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.transaction.session.CartResult;
import com.funnelback.publicui.search.service.IndexRepository;
import com.funnelback.publicui.search.service.ResultsCartRepository;
import com.funnelback.publicui.search.web.controllers.session.ResultsCartController;
import com.funnelback.publicui.test.mock.MockConfigRepository;
import com.funnelback.publicui.test.search.service.session.SessionDaoTest;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
@Transactional // Required for lazy loading
public class ResultsCartControllerTests extends SessionDaoTest {

    private ObjectMapper jsonMapper = new ObjectMapper();
    
    @Autowired
    private ResultsCartController controller;
    
    @Autowired
    private MockConfigRepository configRepository;
    
    @Autowired
    private ResultsCartRepository repository;

    @Override
    public void before() throws Exception {
        configRepository.addCollection(collection);
    }

    @Test
    public void testInvalidCollection() throws IOException {
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.cartClear("invalid", user, response);
        assertEquals(400, response.getStatus());
        
        response = new MockHttpServletResponse();
        controller.cartAdd("invalid", URI.create("funnelback://result/"), user, response);
        assertEquals(400, response.getStatus());

        response = new MockHttpServletResponse();
        controller.cartList("invalid", user, response);
        assertEquals(400, response.getStatus());

        response = new MockHttpServletResponse();
        controller.cartRemove("invalid", URI.create("funnelback://result/"), user, response);
        assertEquals(400, response.getStatus());
    }
    
    @Test
    public void testNoUser() throws IOException {
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.cartClear(collection.getId(), null, response);
        assertEquals(400, response.getStatus());
        
        response = new MockHttpServletResponse();
        controller.cartAdd(collection.getId(), URI.create("funnelback://result/"), null, response);
        assertEquals(400, response.getStatus());

        response = new MockHttpServletResponse();
        controller.cartList(collection.getId(), null, response);
        assertEquals(400, response.getStatus());

        response = new MockHttpServletResponse();
        controller.cartRemove(collection.getId(), URI.create("funnelback://result/"), null, response);
        assertEquals(400, response.getStatus());
    }
    
    @Test
    public void testCartClear() throws IOException {
        for (int i=0; i<3; i++) {
            repository.addToCart(generateRandomCartResult(collection.getId(), user.getId()));
        }
        
        assertEquals(3, repository.getCart(user, collection).size());
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.cartClear(collection.getId(), user, response);

        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertEquals("[]", response.getContentAsString());
        
        assertEquals(0, repository.getCart(user, collection).size());
    }
    
    @Test
    public void testCartClearEmptyCart() throws IOException {
        assertEquals(0, repository.getCart(user, collection).size());
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.cartClear(collection.getId(), user, response);
        
        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertEquals("[]", response.getContentAsString());

        assertEquals(0, repository.getCart(user, collection).size());
    }
    
    @Test
    public void testCartListEmptyList() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.cartList(collection.getId(), user, response);
        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertEquals("[]", response.getContentAsString());        
    }
    
    @Test
    public void testCartList() throws IOException {
        for (int i=0; i<3; i++) {
            repository.addToCart(generateRandomCartResult(collection.getId(), user.getId()));
        }
        assertEquals(3, repository.getCart(user, collection).size());
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.cartList(collection.getId(), user, response);
        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertNotSame("[]", response.getContentAsString());
        assertEquals(
            jsonMapper.writeValueAsString(repository.getCart(user, collection)),
            response.getContentAsString());
    }
    
    @Test
    public void testCartRemove() throws IOException {
        for (int i=0; i<3; i++) {
            repository.addToCart(generateRandomCartResult(collection.getId(), user.getId()));
        }
        List<CartResult> cart = repository.getCart(user, collection); 
        assertEquals(3, cart.size());
        URI uri = repository.getCart(user, collection).get(1).getIndexUrl();
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.cartRemove(collection.getId(), uri, user, response);
        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertNotSame("[]", response.getContentAsString());
        
        cart.remove(1);
        assertEquals(
            jsonMapper.writeValueAsString(cart),
            response.getContentAsString());
        assertEquals(2, repository.getCart(user, collection).size());
    }
    
    @Test
    public void testCartRemoveNonExistent() throws IOException {
        for (int i=0; i<3; i++) {
            repository.addToCart(generateRandomCartResult(collection.getId(), user.getId()));
        }
        assertEquals(3, repository.getCart(user, collection).size());
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.cartRemove(collection.getId(), URI.create("funnelback://non-existent/"), user, response);
        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertNotSame("[]", response.getContentAsString());
        assertEquals(
            jsonMapper.writeValueAsString(repository.getCart(user, collection)),
            response.getContentAsString());
        assertEquals(3, repository.getCart(user, collection).size());
    }
    
    @Test
    public void testCartAdd() throws IOException {
        Result r = new Result();
        r.setCollection(collection.getId());
        r.setIndexUrl("funnelback://result.url/");
        r.setSummary("A summary");
        r.setTitle("A title");
        
        IndexRepository indexRepository = mock(IndexRepository.class);
        when(indexRepository.getResult(any(Collection.class), eq(URI.create("funnelback://result.url/"))))
            .thenReturn(r);
        controller.setIndexRepository(indexRepository);
        
        assertEquals(0, repository.getCart(user, collection).size());
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.cartAdd(collection.getId(), URI.create("funnelback://result.url/"), user, response);
        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertNotSame("[]", response.getContentAsString());
        assertEquals(
            jsonMapper.writeValueAsString(repository.getCart(user, collection)),
            response.getContentAsString());
        assertEquals(1, repository.getCart(user, collection).size());
    }
    
    @Test
    public void testCartAddInvalidResult() throws IOException {
        IndexRepository indexRepository = mock(IndexRepository.class);
        when(indexRepository.getResult(any(Collection.class), eq(URI.create("funnelback://result.url/"))))
            .thenReturn(null);
        controller.setIndexRepository(indexRepository);
        
        assertEquals(0, repository.getCart(user, collection).size());
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.cartAdd(collection.getId(), URI.create("funnelback://result.url/"), user, response);
        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertEquals("[]", response.getContentAsString());
        assertEquals(0, repository.getCart(user, collection).size());
    }

}
