package com.funnelback.publicui.test.search.web.controllers.session;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.common.config.Files;
import com.funnelback.common.config.ProfileId;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.log.CartClickLog;
import com.funnelback.publicui.search.model.transaction.session.CartResult;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.service.ResultsCartRepository;
import com.funnelback.publicui.search.web.controllers.session.CustomCartProcessor;
import com.funnelback.publicui.search.web.controllers.session.ResultsCartProcessController;
import com.funnelback.publicui.test.mock.MockConfigRepository;
import com.funnelback.publicui.test.mock.MockLogService;
import com.funnelback.publicui.test.search.service.session.SessionDaoTest;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class ResultsCartProcessControllerTests extends SessionDaoTest {

    @Autowired
    private ResultsCartProcessController controller;

    @Autowired
    private MockConfigRepository configRepository;

    @Autowired
    private ResultsCartRepository repository;

    @Autowired
    private MockLogService logService;

    @Override
    public void before() throws Exception {
        configRepository.addCollection(collection);
        logService.resetCartLog();
    }

    @Test
    public void testInvalidParameters() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.cartProcess(null, new ProfileId("profile"), user, new MockHttpServletRequest(), response);
        assertEquals(404, response.getStatus());
    }

    @Test
    public void testNoUser() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.cartProcess(collection, new ProfileId("profile"), null, new MockHttpServletRequest(), response);
        assertEquals(404, response.getStatus());
    }

    @Test
    public void testEmptyCart() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        ModelAndView mav = controller.cartProcess(collection, new ProfileId("profile"), user, new MockHttpServletRequest(),
            response);

        assertEquals(200, response.getStatus());
        assertEquals("conf/" + collection.getId() + "/profile/" + Files.CART_PROCESS_PREFIX, mav.getViewName());
        assertEquals(user, mav.getModel().get("user"));
        assertEquals(collection, mav.getModel().get("collection"));
        assertEquals(0, ((List<CartResult>) mav.getModel().get("cart")).size());
    }

    @Test
    public void testEmptyCartProcessingNotLogged() throws Exception {
        // Given an empty cart

        // When its results are processed
        MockHttpServletResponse response = new MockHttpServletResponse();
        ModelAndView mav = controller.cartProcess(collection, new ProfileId("profile"), user, new MockHttpServletRequest(),
            response);

        // Then interactions are not logged
        assertEquals(0, logService.getCartLogs().size());
    }

    @Test
    public void testNonEmptyCart() throws Exception {
        for (int i = 0; i < 3; i++) {
            repository.addToCart(collection.getId(), generateRandomCartResult(collection.getId() + "-comp", user.getId()));
        }

        MockHttpServletResponse response = new MockHttpServletResponse();
        ModelAndView mav = controller.cartProcess(collection, new ProfileId("profile"), user, new MockHttpServletRequest(),
            response);

        assertEquals(200, response.getStatus());
        assertEquals("conf/" + collection.getId() + "/profile/" + Files.CART_PROCESS_PREFIX, mav.getViewName());
        assertEquals(user, mav.getModel().get("user"));
        assertEquals(collection, mav.getModel().get("collection"));
        assertEquals(3, ((List<CartResult>) mav.getModel().get("cart")).size());
    }

    @Test
    public void testNonEmptyCartProcessingIsLogged() throws Exception {
        // Given a non empty cart
        for (int i = 0; i < 3; i++) {
            repository.addToCart(collection.getId(), generateRandomCartResult(collection.getId() + "-comp", user.getId()));
        }

        // When the cart results are processed
        MockHttpServletResponse response = new MockHttpServletResponse();
        ModelAndView mav = controller.cartProcess(collection, new ProfileId("profile"), user, new MockHttpServletRequest(),
            response);

        // The interaction is logged
        assertEquals(3, logService.getCartLogs().size());
        for (CartClickLog log : logService.getCartLogs()) {
            assertEquals(CartClickLog.Type.PROCESS_CART, log.getType());
        }
    }

    @Test
    public void testCustomProcessClassSimple() throws Exception {
        for (int i = 0; i < 3; i++) {
            repository.addToCart(collection.getId(), generateRandomCartResult(collection.getId() + "-comp", user.getId()));
        }

        collection.setCartProcessClass(SimpleCustomCartProcessor.class);
        MockHttpServletResponse response = new MockHttpServletResponse();
        ModelAndView mav = controller.cartProcess(collection, new ProfileId("profile"), user, new MockHttpServletRequest(),
            response);

        assertEquals(200, response.getStatus());
        assertEquals("conf/" + collection.getId() + "/profile/a-view", mav.getViewName());
        assertEquals(user, mav.getModel().get("user"));
        assertEquals(collection, mav.getModel().get("collection"));
        assertEquals("unit-value", mav.getModel().get("unit-test"));
        assertEquals(3, ((List<CartResult>) mav.getModel().get("cart")).size());
    }

    @Test
    public void testCustomProcessClassComplex() throws Exception {
        for (int i = 0; i < 3; i++) {
            repository.addToCart(collection.getId(), generateRandomCartResult(collection.getId() + "-comp", user.getId()));
        }

        collection.setCartProcessClass(ComplexCustomCartProcessor.class);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("an-attribute", "42");
        ModelAndView mav = controller.cartProcess(collection, new ProfileId("profile"), user, request, response);

        assertEquals(200, response.getStatus());
        assertEquals("conf/" + collection.getId() + "/profile/a-view", mav.getViewName());
        assertEquals(user, mav.getModel().get("user"));
        assertEquals(collection, mav.getModel().get("collection"));
        assertEquals("unit-value", mav.getModel().get("unit-test"));
        assertEquals("42", response.getHeader("X-Unit-Test"));
        assertEquals(3, ((List<CartResult>) mav.getModel().get("cart")).size());
        assertEquals(collection.getId() + "-comp", ((List<CartResult>) mav.getModel().get("cart")).get(0).getCollection());
    }

    public static class SimpleCustomCartProcessor extends CustomCartProcessor {
        @Override
        public ModelAndView process(Collection collection, SearchUser user, List<CartResult> cart) {
            ModelAndView mav = super.process(collection, user, cart);
            mav.addObject("unit-test", "unit-value");
            mav.setViewName("a-view");

            return mav;
        }
    }

    public static class ComplexCustomCartProcessor extends SimpleCustomCartProcessor {
        @Override
        public ModelAndView process(Collection collection, SearchUser user, List<CartResult> cart,
            HttpServletRequest request, HttpServletResponse response) {

            response.addHeader("X-Unit-Test", (String) request.getAttribute("an-attribute"));
            return super.process(collection, user, cart);
        }
    }

}
