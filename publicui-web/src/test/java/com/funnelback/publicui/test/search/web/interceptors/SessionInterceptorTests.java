package com.funnelback.publicui.test.search.web.interceptors;

import static com.funnelback.config.keys.Keys.FrontEndKeys;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.UUID;

import javax.servlet.http.Cookie;

import com.funnelback.config.configtypes.service.DefaultServiceConfig;
import com.funnelback.config.configtypes.service.ServiceConfig;
import com.funnelback.config.data.InMemoryConfigData;
import com.funnelback.config.data.environment.NoConfigEnvironment;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
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
    private ServiceConfig serviceConfig;

    @Before
    public void before() throws Exception {
        collection = new Collection("dummy",
            new NoOptionsConfig(new File("src/test/resources/dummy-search_home"), "dummy"));
        ConfigRepository configRepository = Mockito.mock(ConfigRepository.class);

        serviceConfig = new DefaultServiceConfig(new InMemoryConfigData(Maps.newHashMap()), new NoConfigEnvironment());

        Mockito
            .when(configRepository.getServiceConfig("dummy","_default"))
            .thenReturn(serviceConfig);
        Mockito
            .when(configRepository.getCollection("dummy"))
            .thenReturn(collection);

        interceptor = new SessionInterceptor();
        interceptor.setConfigRepository(configRepository);
        
        req = new MockHttpServletRequest();
        req.addParameter("collection", "dummy");
        
        resp = new MockHttpServletResponse();
        
        enableFeatures(false);
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
    public void testCookieNewUser() throws Exception {
        enableFeatures(true);
        
        serviceConfig.set(FrontEndKeys.ModernUi.Session.TIMEOUT, 123);
        interceptor.preHandle(req, resp, null);

        assertNull(req.getSession(false));
        
        assertEquals(1, resp.getCookies().length);
        assertEquals(SessionInterceptor.USER_ID_COOKIE_NAME, resp.getCookies()[0].getName());
        UUID.fromString(resp.getCookies()[0].getValue());
        
        assertEquals(
            UUID.fromString(resp.getCookies()[0].getValue()),
            UUID.fromString((String) req.getAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE)));
        
        int maxAge = resp.getCookies()[0].getMaxAge();
        assertEquals("Ensure the cookie max age is set to the ui.modern.session.timeout", 123, maxAge);
    }

    @Test
    public void testCookieReturningUser() throws Exception {
        enableFeatures(true);
        
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
        enableFeatures(true);
        
        interceptor.preHandle(req, resp, null);

        assertNotNull(req.getAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE));
        UUID uuidSession = UUID.fromString((String) req.getAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE));
        
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
    public void testCookieReturningUserFromCookie() throws Exception {
        enableFeatures(true);

        UUID uuid = UUID.randomUUID();
        req.setCookies(new Cookie(SessionInterceptor.USER_ID_COOKIE_NAME, uuid.toString()));

        interceptor.preHandle(req, resp, null);

        assertNotNull(req.getAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE));
        assertEquals(
            uuid,
            UUID.fromString((String) req.getAttribute(SessionInterceptor.SEARCH_USER_ID_ATTRIBUTE)));
        
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
        enableFeatures(true);

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
    
    @Test
    public void testSomeCookiePresent() throws Exception {
        enableFeatures(true);
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
    public void testInvalidCookieAttribute() throws Exception {
        enableFeatures(true);
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

    private void enableFeatures(boolean session) {
        serviceConfig.set(FrontEndKeys.ModernUi.Session.SESSION, session);
    }
    
}
