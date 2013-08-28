package com.funnelback.publicui.test.search.web.interceptors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.UUID;

import javax.servlet.http.Cookie;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.web.interceptors.SessionInterceptor;
import com.funnelback.publicui.test.mock.MockConfigRepository;

public class SessionInterceptorTests {

    private SessionInterceptor interceptor;
    private Collection collection;
    
    private MockHttpServletRequest req;
    private MockHttpServletResponse resp;
    
    @Before
    public void before() throws FileNotFoundException {
        collection = new Collection("dummy",
            new NoOptionsConfig(new File("src/test/resources/dummy-search_home"), "dummy"));
        MockConfigRepository configRepository = new MockConfigRepository();
        configRepository.addCollection(collection);
        
        interceptor = new SessionInterceptor();
        interceptor.setConfigRepository(configRepository);
        
        req = new MockHttpServletRequest();
        req.addParameter("collection", "dummy");
        
        resp = new MockHttpServletResponse();
        
        enableFeatures(false,  false);
    }
    
    @Test
    public void testAfterMethodsShouldBeNoOps() throws Exception {
        interceptor.afterCompletion(null, null, null, null);
        interceptor.postHandle(null, null, null, null);
    }
    
    @Test
    public void testNoCollection() throws Exception {
        req.removeAllParameters();
        interceptor.preHandle(req, resp, null);
        
        assertNull(req.getSession(false));
        assertFalse(req.getAttributeNames().hasMoreElements());
        assertEquals(0, resp.getCookies().length);
    }

    @Test
    public void testInvalidCollection() throws Exception {
        req.setParameter("collection", "invalid");
        interceptor.preHandle(req, resp, null);
        
        assertNull(req.getSession(false));
        assertFalse(req.getAttributeNames().hasMoreElements());
        assertEquals(0, resp.getCookies().length);
    }

    @Test
    public void testNoSessionNoCookies() throws Exception {
        interceptor.preHandle(req, resp, null);
        
        assertNull(req.getSession(false));
        assertFalse(req.getAttributeNames().hasMoreElements());
        assertEquals(0, resp.getCookies().length);
    }
    
    @Test
    public void testSessionNewUser() throws Exception {
        enableFeatures(true, false);
        
        interceptor.preHandle(req, resp, null);

        assertFalse(req.getAttributeNames().hasMoreElements());
        assertEquals(0, resp.getCookies().length);
        
        assertNotNull(req.getSession(false));
        assertNotNull(req.getSession().getAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE));
        // Should be valid UUID
        UUID.fromString((String) req.getSession().getAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE));
    }
    
    @Test
    public void testSessionReturningUser() throws Exception {
        enableFeatures(true, false);
        
        UUID uuid = UUID.randomUUID();
        req.getSession(true).setAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE, uuid.toString());
        
        collection.getConfiguration().setValue(Keys.ModernUI.SESSION, "true");
        
        interceptor.preHandle(req, resp, null);

        assertFalse(req.getAttributeNames().hasMoreElements());
        assertEquals(0, resp.getCookies().length);
        
        assertNotNull(req.getSession(false));
        assertNotNull(req.getSession().getAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE));
        assertEquals(
            uuid,
            UUID.fromString((String) req.getSession().getAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE)));
    }
    
    @Test
    public void testCookieNewUser() throws Exception {
        enableFeatures(false, true);
        
        interceptor.preHandle(req, resp, null);

        assertNull(req.getSession(false));
        
        assertEquals(1, resp.getCookies().length);
        assertEquals(SessionInterceptor.USER_ID_COOKIE_NAME, resp.getCookies()[0].getName());
        UUID.fromString(resp.getCookies()[0].getValue());
        
        assertEquals(
            UUID.fromString(resp.getCookies()[0].getValue()),
            UUID.fromString((String) req.getAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE)));
    }

    @Test
    public void testCookieReturningUser() throws Exception {
        enableFeatures(false, true);
        
        UUID uuid = UUID.randomUUID();
        req.setCookies(new Cookie(SessionInterceptor.USER_ID_COOKIE_NAME, uuid.toString()));
        
        interceptor.preHandle(req, resp, null);

        assertNull(req.getSession(false));
        assertEquals(1, resp.getCookies().length);
        assertEquals(SessionInterceptor.USER_ID_COOKIE_NAME, resp.getCookies()[0].getName());
        assertEquals(
            uuid,
            UUID.fromString(resp.getCookies()[0].getValue()));
        assertEquals(
            uuid,
            UUID.fromString((String) req.getAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE)));
    }
    
    @Test
    public void testSessionAndCookieNewUser() throws Exception {
        enableFeatures(true, true);
        
        interceptor.preHandle(req, resp, null);

        assertNotNull(req.getSession(false));
        assertNotNull(req.getSession().getAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE));
        UUID uuidSession = UUID.fromString((String) req.getSession().getAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE));
        
        assertEquals(1, resp.getCookies().length);
        assertEquals(SessionInterceptor.USER_ID_COOKIE_NAME, resp.getCookies()[0].getName());
        assertEquals(
            uuidSession,
            UUID.fromString(resp.getCookies()[0].getValue()));
        assertEquals(
            uuidSession,
            UUID.fromString((String) req.getAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE)));
    }
    
    @Test
    public void testSessionAndCookieReturningUserFromCookie() throws Exception {
        enableFeatures(true, true);

        UUID uuid = UUID.randomUUID();
        req.setCookies(new Cookie(SessionInterceptor.USER_ID_COOKIE_NAME, uuid.toString()));

        interceptor.preHandle(req, resp, null);

        assertNotNull(req.getSession(false));
        assertNotNull(req.getSession().getAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE));
        assertEquals(
            uuid,
            UUID.fromString((String) req.getSession().getAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE)));
        
        assertEquals(1, resp.getCookies().length);
        assertEquals(SessionInterceptor.USER_ID_COOKIE_NAME, resp.getCookies()[0].getName());
        assertEquals(
            uuid,
            UUID.fromString(resp.getCookies()[0].getValue()));
        assertEquals(
            uuid,
            UUID.fromString((String) req.getAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE)));
    }

    @Test
    public void testSessionAndCookieReturningUserFromSession() throws Exception {
        enableFeatures(true, true);

        UUID uuid = UUID.randomUUID();
        req.getSession(true).setAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE, uuid.toString());

        interceptor.preHandle(req, resp, null);

        assertNotNull(req.getSession(false));
        assertNotNull(req.getSession().getAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE));
        assertEquals(
            uuid,
            UUID.fromString((String) req.getSession().getAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE)));
        
        assertEquals(1, resp.getCookies().length);
        assertEquals(SessionInterceptor.USER_ID_COOKIE_NAME, resp.getCookies()[0].getName());
        assertEquals(
            uuid,
            UUID.fromString(resp.getCookies()[0].getValue()));
        assertEquals(
            uuid,
            UUID.fromString((String) req.getAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE)));
    }

    @Test
    public void testSessionAndCookieReturningUserFromSessionAndCookie() throws Exception {
        enableFeatures(true, true);

        UUID uuid = UUID.randomUUID();
        req.setCookies(new Cookie(SessionInterceptor.USER_ID_COOKIE_NAME, uuid.toString()));
        req.getSession(true).setAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE, uuid.toString());

        interceptor.preHandle(req, resp, null);

        assertNotNull(req.getSession(false));
        assertNotNull(req.getSession().getAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE));
        assertEquals(
            uuid,
            UUID.fromString((String) req.getSession().getAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE)));
        
        assertEquals(1, resp.getCookies().length);
        assertEquals(SessionInterceptor.USER_ID_COOKIE_NAME, resp.getCookies()[0].getName());
        assertEquals(
            uuid,
            UUID.fromString(resp.getCookies()[0].getValue()));
        assertEquals(
            uuid,
            UUID.fromString((String) req.getAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE)));
    }

    /**
     * Session value should take precedence over cookie value
     * @throws Exception
     */
    @Test
    public void testSessionAndCookieReturningUserFromSessionAndCookieDifferentValues() throws Exception {
        enableFeatures(true, true);

        UUID uuidCookie = UUID.randomUUID();
        req.setCookies(new Cookie(SessionInterceptor.USER_ID_COOKIE_NAME, uuidCookie.toString()));
        UUID uuidSession = UUID.randomUUID();
        req.getSession(true).setAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE, uuidSession.toString());

        interceptor.preHandle(req, resp, null);

        assertNotNull(req.getSession(false));
        assertNotNull(req.getSession().getAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE));
        assertEquals(
            uuidSession,
            UUID.fromString((String) req.getSession().getAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE)));
        
        assertEquals(1, resp.getCookies().length);
        assertEquals(SessionInterceptor.USER_ID_COOKIE_NAME, resp.getCookies()[0].getName());
        assertEquals(
            uuidSession,
            UUID.fromString(resp.getCookies()[0].getValue()));
        assertEquals(
            uuidSession,
            UUID.fromString((String) req.getAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE)));
    }
    
    @Test
    public void testSessionPresentButNoAttribute() throws Exception {
        enableFeatures(true, false);
        req.getSession(true).setAttribute("test", "test");
        
        interceptor.preHandle(req, resp, null);

        assertFalse(req.getAttributeNames().hasMoreElements());
        assertEquals(0, resp.getCookies().length);
        
        assertNotNull(req.getSession(false));
        assertNotNull(req.getSession().getAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE));
        // Should be valid UUID
        UUID.fromString((String) req.getSession().getAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE));        
    }
    
    @Test
    public void testSomeCookiePresent() throws Exception {
        enableFeatures(false, true);
        req.setCookies(new Cookie("a-cookie", "not a uuid"), new Cookie("another-one", "still not a uuid"));
        
        interceptor.preHandle(req, resp, null);
        
        assertNull(req.getSession(false));
        
        assertEquals(1, resp.getCookies().length);
        assertEquals(SessionInterceptor.USER_ID_COOKIE_NAME, resp.getCookies()[0].getName());
        UUID.fromString(resp.getCookies()[0].getValue());
        
        assertEquals(
            UUID.fromString(resp.getCookies()[0].getValue()),
            UUID.fromString((String) req.getAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE)));
    }

    @Test
    public void testInvalidSessionAttribute() throws Exception {
        enableFeatures(true, false);
        req.getSession(true).setAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE, "not a uuid");
        
        interceptor.preHandle(req, resp, null);

        assertFalse(req.getAttributeNames().hasMoreElements());
        assertEquals(0, resp.getCookies().length);
        
        assertNotNull(req.getSession(false));
        assertNotNull(req.getSession().getAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE));
        // Should be valid UUID
        UUID.fromString((String) req.getSession().getAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE));        
    }

    @Test
    public void testInvalidCookieAttribute() throws Exception {
        enableFeatures(false, true);
        req.setCookies(new Cookie(SessionInterceptor.USER_ID_COOKIE_NAME, "not a uuid"));
        
        interceptor.preHandle(req, resp, null);
        
        assertNull(req.getSession(false));
        
        assertEquals(1, resp.getCookies().length);
        assertEquals(SessionInterceptor.USER_ID_COOKIE_NAME, resp.getCookies()[0].getName());
        UUID.fromString(resp.getCookies()[0].getValue());
        
        assertEquals(
            UUID.fromString(resp.getCookies()[0].getValue()),
            UUID.fromString((String) req.getAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE)));
    }

    private void enableFeatures(boolean session, boolean cookie) {
        collection.getConfiguration().setValue(
            Keys.ModernUI.SESSION,
            Boolean.toString(session));
        
        collection.getConfiguration().setValue(
            Keys.ModernUI.Session.SET_USERID_COOKIE,
            Boolean.toString(cookie));
    }
    
}
