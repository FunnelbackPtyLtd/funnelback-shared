package com.funnelback.publicui.test.search.web.controllers.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.log.CartClickLog;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.transaction.session.CartResult;
import com.funnelback.publicui.search.service.IndexRepository;
import com.funnelback.publicui.search.service.ResultsCartRepository;
import com.funnelback.publicui.search.web.controllers.session.ResultsCartController;
import com.funnelback.publicui.test.mock.MockConfigRepository;
import com.funnelback.publicui.test.mock.MockLogService;
import com.funnelback.publicui.test.search.service.session.SessionDaoTest;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
//Required for lazy loading
@Transactional
public class ResultsCartControllerTests extends SessionDaoTest {

    private ObjectMapper jsonMapper = new ObjectMapper();

    @Autowired
    private ResultsCartController controller;

    @Autowired
    private MockConfigRepository configRepository;

    @Autowired
    private ResultsCartRepository repository;

    @Autowired
    private MockLogService logService;

    @Override
    public void before() throws Exception {
        logService.resetCartLog();
        configRepository.addCollection(collection);
    }

    @Test
    public void testInvalidCollection() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.cartClear(null, user, request, response);
        assertEquals(404, response.getStatus());

        response = new MockHttpServletResponse();
        controller.cartAdd(null, URI.create("funnelback://result/"), user, request, response);
        assertEquals(404, response.getStatus());

        response = new MockHttpServletResponse();
        controller.cartList(null, user, response);
        assertEquals(404, response.getStatus());

        response = new MockHttpServletResponse();
        controller.cartRemove(null, URI.create("funnelback://result/"), user, request, response);
        assertEquals(404, response.getStatus());
    }

    @Test
    public void testNoUser() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.cartClear(collection, null, request, response);
        assertEquals(404, response.getStatus());

        response = new MockHttpServletResponse();
        controller.cartAdd(collection, URI.create("funnelback://result/"), null, request, response);
        assertEquals(404, response.getStatus());

        response = new MockHttpServletResponse();
        controller.cartList(collection, null, response);
        assertEquals(404, response.getStatus());

        response = new MockHttpServletResponse();
        controller.cartRemove(collection, URI.create("funnelback://result/"), null, request, response);
        assertEquals(404, response.getStatus());
    }

    @Test
    public void testCartClear() throws IOException {
        for (int i = 0; i < 3; i++) {
            repository.addToCart(generateRandomCartResult(collection.getId(), user.getId()));
        }

        assertEquals(3, repository.getCart(user, collection).size());

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.cartClear(collection, user, request, response);

        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertEquals("[]", response.getContentAsString());

        assertEquals(0, repository.getCart(user, collection).size());
    }

    @Test
    public void testCartClearCreatesLogs() throws IOException {
        // Given a cart with results
        for (int i = 0; i < 3; i++) {
            repository.addToCart(generateRandomCartResult(collection.getId(), user.getId()));
        }

        assertEquals(3, repository.getCart(user, collection).size());

        // When the cart is cleared
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.cartClear(collection, user, request, response);

        // Then cart clear logs must be created
        assertEquals(0, repository.getCart(user, collection).size());
        assertEquals(3, logService.getCartLogs().size());
        for (CartClickLog log : logService.getCartLogs()) {
            assertEquals(CartClickLog.Type.CLEAR_CART, log.getType());
        }
    }

    @Test
    public void testCartClearEmptyCart() throws IOException {
        assertEquals(0, repository.getCart(user, collection).size());

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.cartClear(collection, user, request, response);

        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertEquals("[]", response.getContentAsString());

        assertEquals(0, repository.getCart(user, collection).size());
        assertEquals(0, logService.getCartLogs().size());
    }

    @Test
    public void testCartClearEmptyCartDoesntCreateLog() throws IOException {
        // Given a empty cart
        assertEquals(0, repository.getCart(user, collection).size());

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // When cart is cleared
        controller.cartClear(collection, user, request, response);

        // Then cart log must not be created
        assertEquals(0, repository.getCart(user, collection).size());
        assertEquals(0, logService.getCartLogs().size());
    }

    @Test
    public void testCartListEmptyList() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.cartList(collection, user, response);
        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertEquals("[]", response.getContentAsString());
    }

    @Test
    public void testCartList() throws IOException {
        for (int i = 0; i < 3; i++) {
            repository.addToCart(generateRandomCartResult(collection.getId(), user.getId()));
        }
        assertEquals(3, repository.getCart(user, collection).size());

        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.cartList(collection, user, response);
        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertNotSame("[]", response.getContentAsString());
        assertEquals(jsonMapper.writeValueAsString(repository.getCart(user, collection)), response.getContentAsString());
    }

    @Test
    public void testCartRemove() throws IOException {
        for (int i = 0; i < 3; i++) {
            repository.addToCart(generateRandomCartResult(collection.getId(), user.getId()));
        }
        List<CartResult> cart = repository.getCart(user, collection);
        assertEquals(3, cart.size());
        URI uri = repository.getCart(user, collection).get(1).getIndexUrl();

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.cartRemove(collection, uri, user, request, response);
        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertNotSame("[]", response.getContentAsString());

        cart.remove(1);
        assertEquals(jsonMapper.writeValueAsString(cart), response.getContentAsString());
        assertEquals(2, repository.getCart(user, collection).size());
        assertEquals(1, logService.getCartLogs().size());
        assertEquals(CartClickLog.Type.REMOVE_FROM_CART, logService.getCartLogs().get(0).getType());
    }

    @Test
    public void testCartRemoveLogCreated() throws IOException {

        // Given a cart with results
        for (int i = 0; i < 3; i++) {
            repository.addToCart(generateRandomCartResult(collection.getId(), user.getId()));
        }
        List<CartResult> cart = repository.getCart(user, collection);
        assertEquals(3, cart.size());
        URI uri = repository.getCart(user, collection).get(1).getIndexUrl();

        // When cart is removed
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.cartRemove(collection, uri, user, request, response);

        cart.remove(1);

        // Then a cart removal log must be created
        assertEquals(CartClickLog.Type.REMOVE_FROM_CART, logService.getCartLogs().get(0).getType());
    }

    @Test
    public void testCartRemoveNonExistent() throws IOException {
        for (int i = 0; i < 3; i++) {
            repository.addToCart(generateRandomCartResult(collection.getId(), user.getId()));
        }
        assertEquals(3, repository.getCart(user, collection).size());

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.cartRemove(collection, URI.create("funnelback://non-existent/"), user, request, response);
        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertNotSame("[]", response.getContentAsString());
        assertEquals(jsonMapper.writeValueAsString(repository.getCart(user, collection)), response.getContentAsString());
        assertEquals(3, repository.getCart(user, collection).size());
        assertEquals(1, logService.getCartLogs().size());
    }


    @Test
    public void testCartRemoveNonExistentCreatesLog() throws IOException {
        // Given a cart with results
        for (int i = 0; i < 3; i++) {
            repository.addToCart(generateRandomCartResult(collection.getId(), user.getId()));
        }
        assertEquals(3, repository.getCart(user, collection).size());

       // When non existent result is removed
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.cartRemove(collection, URI.create("funnelback://non-existent/"), user, request, response);

        // Then cart removal must be logged
        assertEquals(1, logService.getCartLogs().size());
        assertEquals(CartClickLog.Type.REMOVE_FROM_CART, logService.getCartLogs().get(0).getType());
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

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.cartAdd(collection, URI.create("funnelback://result.url/"), user, request, response);
        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertNotSame("[]", response.getContentAsString());
        assertEquals(jsonMapper.writeValueAsString(repository.getCart(user, collection)), response.getContentAsString());
        assertEquals(1, repository.getCart(user, collection).size());
        assertEquals(1, logService.getCartLogs().size());
        assertEquals(CartClickLog.Type.ADD_TO_CART, logService.getCartLogs().get(0).getType());
    }


    @Test
    public void testCartAddCreatesLogs() throws IOException {
        // Given a result
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

        // When added to a cart
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.cartAdd(collection, URI.create("funnelback://result.url/"), user, request, response);

        // Then the creation is logged
        assertEquals(CartClickLog.Type.ADD_TO_CART, logService.getCartLogs().get(0).getType());
    }

    @Test
    public void testCartAddInvalidResult() throws IOException {
        IndexRepository indexRepository = mock(IndexRepository.class);
        when(indexRepository.getResult(any(Collection.class), eq(URI.create("funnelback://result.url/")))).thenReturn(
            null);
        controller.setIndexRepository(indexRepository);

        assertEquals(0, repository.getCart(user, collection).size());

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.cartAdd(collection, URI.create("funnelback://result.url/"), user, request, response);
        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertEquals("[]", response.getContentAsString());
        assertEquals(0, repository.getCart(user, collection).size());
        assertEquals(0, logService.getCartLogs().size());
    }


    @Test
    public void testCartAddInvalidResultIsNotLogged() throws IOException {
        // Given an invalid result
        IndexRepository indexRepository = mock(IndexRepository.class);
        when(indexRepository.getResult(any(Collection.class), eq(URI.create("funnelback://result.url/")))).thenReturn(
            null);
        controller.setIndexRepository(indexRepository);

        assertEquals(0, repository.getCart(user, collection).size());

        // When it is added to cart
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.cartAdd(collection, URI.create("funnelback://result.url/"), user, request, response);

        // Then it is not logged
        assertEquals(0, logService.getCartLogs().size());
    }

}
