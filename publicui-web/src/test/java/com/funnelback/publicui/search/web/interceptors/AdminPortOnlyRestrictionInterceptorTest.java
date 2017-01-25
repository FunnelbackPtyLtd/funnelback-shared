package com.funnelback.publicui.search.web.interceptors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.Keys;
import com.funnelback.config.configtypes.server.ServerConfigReadOnly;
import com.funnelback.config.keys.Keys.ServerKeys;
import com.funnelback.publicui.search.service.ConfigRepository;

public class AdminPortOnlyRestrictionInterceptorTest {

    private AdminPortOnlyRestrictionInterceptor interceptor;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    
    @Before
    public void before() {
        request = new MockHttpServletRequest();
        request.setServerName("example.org");
        request.setRequestURI("/uri");
        request.setQueryString("a=b&c=d");
        response = new MockHttpServletResponse();
        
        
        ServerConfigReadOnly serverConfig = mock(ServerConfigReadOnly.class);
        when(serverConfig.get(ServerKeys.Urls.ADMIN_PORT)).thenReturn(443);
        when(serverConfig.get(ServerKeys.Jetty.JETTY_ADMIN_PORT)).thenReturn(8443);
        when(serverConfig.get(ServerKeys.Urls.DEVELOPMENT_PORT)).thenReturn(8080);
        
        ConfigRepository configRepository = Mockito.mock(ConfigRepository.class);
        when(configRepository.getServerConfig()).thenReturn(serverConfig);
        interceptor = new AdminPortOnlyRestrictionInterceptor();
        interceptor.setConfigRepository(configRepository);
    }
    
    @Test
    public void testAdminPort() throws Exception {
        request.setLocalPort(8443);
        
        Assert.assertTrue(interceptor.preHandle(request, response, null));
        Assert.assertNull(response.getRedirectedUrl());
    }
    
    @Test
    public void testAdditionalPort() throws Exception {
        request.setLocalPort(8080);
        
        Assert.assertTrue(interceptor.preHandle(request, response, null));
        Assert.assertNull(response.getRedirectedUrl());
    }

    @Test
    public void testNonPermittedPort() throws Exception {
        request.setLocalPort(80);
        
        Assert.assertFalse(interceptor.preHandle(request, response, null));
        Assert.assertEquals("https://example.org:443/uri?a=b&c=d", response.getRedirectedUrl());
    }

    
    
}
