package com.funnelback.publicui.test.search.web.controllers.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.web.controllers.session.SessionController;
import com.funnelback.publicui.search.web.interceptors.SessionInterceptor;

public class SessionControllerTest {

    private SessionController controller;
    private MockHttpServletRequest request;
    private UUID uuid;
    
    @Before
    public void before() {
        controller = new SessionController() {};
        request = new MockHttpServletRequest();
        uuid = UUID.randomUUID();
    }
    
    @Test
    public void testNoUser() {
        assertNull(controller.getSearchUser(request));
    }

    @Test
    public void testNullOrInvalidSessionAttribute() {
        HttpSession s = request.getSession(true);
        s.setAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE, null);
        assertNull(controller.getSearchUser(request));

        s.setAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE, "not a uuid");
        assertNull(controller.getSearchUser(request));
    }

    @Test
    public void testSession() {
        HttpSession s = request.getSession(true);
        s.setAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE, uuid.toString());
        
        SearchUser user = controller.getSearchUser(request);
        assertNotNull(user);
        assertEquals(uuid.toString(), user.getId());
    }
    
    @Test
    public void testNullOrInvalidCookie() {
        request.setCookies(new Cookie(SessionInterceptor.USER_ID_COOKIE_NAME, null));
        assertNull(controller.getSearchUser(request));

        request.setCookies(new Cookie(SessionInterceptor.USER_ID_COOKIE_NAME, "not a uuid"));
        assertNull(controller.getSearchUser(request));
    }

    @Test
    public void testCookie() {
        request.setCookies(new Cookie(SessionInterceptor.USER_ID_COOKIE_NAME, uuid.toString()));
        
        SearchUser user = controller.getSearchUser(request);
        assertNotNull(user);
        assertEquals(uuid.toString(), user.getId());
    }

    @Test
    public void testNullOrInvalidRequestAttribute() {
        request.setAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE, null);
        assertNull(controller.getSearchUser(request));

        request.setAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE, "not a uuid");
        assertNull(controller.getSearchUser(request));
    }

    @Test
    public void testRequestAttribute() {
        request.setAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE, uuid.toString());
        
        SearchUser user = controller.getSearchUser(request);
        assertNotNull(user);
        assertEquals(uuid.toString(), user.getId());
    }
    

}
