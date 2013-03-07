package com.funnelback.publicui.test.search.service.auth;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.funnelback.publicui.search.service.auth.AuthTokenManager;
import com.funnelback.publicui.search.service.auth.DefaultAuthTokenManager;

public class DefaultAuthTokenOracleTest {

    private AuthTokenManager authTokenManager;

    @Before
    public void setup() {
        authTokenManager = new DefaultAuthTokenManager();        
    }
    
    @Test
    public void testGetToken() {
        String url = "http://docs.funnelback.com/11.4/search_form_tags.html";
        String auth = "M7kPcuudrqqoyQykGMuAoQ";
        String secret = "2995d287ccd77febdb1f12529ea9749d";
        
        String genToken = authTokenManager.getToken(url, secret);
        
        assertEquals(auth, genToken);
    }

    @Test
    public void testCheckToken() {
        String url = "http://docs.funnelback.com/11.4/search_form_tags.html";
        String auth = "M7kPcuudrqqoyQykGMuAoQ";
        String secret = "2995d287ccd77febdb1f12529ea9749d";

        assertTrue(authTokenManager.checkToken(auth, url, secret));
    }
    
    @Test
    public void testCheckTokenInvalidToken() {
        String url = "http://docs.funnelback.com/11.4/search_form_tags.html";
        String auth = "QDYvtdztVif3su3m6kY4gw";
        String secret = "2995d287ccd77febdb1f12529ea9749d";

        assertFalse(authTokenManager.checkToken(auth, url, secret));
    }
    
    @Test
    public void testGetOtherToken() {

        String url = "http://docs.funnelback.com/search_forms.html";
        String auth = "QDYvtdztVif3su3m6kY4gw";
        String secret = "2995d287ccd77febdb1f12529ea9749d";
        
        String genToken = authTokenManager.getToken(url, secret);
        
        assertEquals(auth, genToken);
    }

}
