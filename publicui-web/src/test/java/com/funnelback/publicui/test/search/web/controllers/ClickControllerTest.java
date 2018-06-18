package com.funnelback.publicui.test.search.web.controllers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.*;
import static org.mockito.Matchers.*;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.codahale.metrics.MetricRegistry;
import com.funnelback.common.config.Config;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.common.config.ProfileId;
import com.funnelback.common.config.DefaultValues.Metrics;
import com.funnelback.config.configtypes.server.ServerConfigReadOnly;
import com.funnelback.config.configtypes.service.ServiceConfig;
import com.funnelback.config.keys.Keys.FrontEndKeys;
import com.funnelback.config.keys.Keys.ServerKeys;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.log.ClickLog;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.transaction.session.ClickHistory;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.IndexRepository;
import com.funnelback.publicui.search.service.SearchHistoryRepository;
import com.funnelback.publicui.search.service.auth.AuthTokenManager;
import com.funnelback.publicui.search.service.log.LogService;
import com.funnelback.publicui.search.web.controllers.ClickController;

public class ClickControllerTest {

    @Test
    public void testExceptionStillRedirects() throws IOException {
        AuthTokenManager authTokenManager = Mockito.mock(AuthTokenManager.class);
        Mockito.when(authTokenManager.checkToken(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        
        ClickController controller = new ClickController();
        controller.setAuthTokenManager(authTokenManager);
        
        ConfigRepository configRepository = mock(ConfigRepository.class);
        ServerConfigReadOnly serverConfig = mock(ServerConfigReadOnly.class);
        when(configRepository.getServerConfig()).thenReturn(serverConfig);
        controller.setConfigRepository(configRepository);
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        Collection c = new Collection("test", Mockito.mock(Config.class));
        Mockito.when(c.getConfiguration().value(Keys.REQUEST_ID_TO_LOG, DefaultValues.REQUEST_ID_TO_LOG.toString()))
            .thenReturn(DefaultValues.REQUEST_ID_TO_LOG.toString());
        
        // Pass a NULL request to cause a NPE
        controller.redirect(null, response, c, ClickLog.Type.CLICK, 0, null, URI.create("http://redirected.com/"), null, "token", false, null, null);
        
        Assert.assertEquals("http://redirected.com/", response.getRedirectedUrl());
    }
    
    @Test
    public void testExceptionInTokenDoesNotRedirect() throws IOException {
        AuthTokenManager authTokenManager = Mockito.mock(AuthTokenManager.class);
        Mockito.when(authTokenManager.checkToken(Mockito.any(), Mockito.any(), Mockito.any())).thenThrow(new IllegalStateException("unit-test"));
        
        ClickController controller = new ClickController();
        controller.setAuthTokenManager(authTokenManager);
        
        ConfigRepository configRepository = mock(ConfigRepository.class);
        ServerConfigReadOnly serverConfig = mock(ServerConfigReadOnly.class);
        when(configRepository.getServerConfig()).thenReturn(serverConfig);
        controller.setConfigRepository(configRepository);
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        try {
            // We should not redirect if the authentication token caused
            // an error, as it could be used as a way to bypass authentication
            // by crafting an exception-causing token
            controller.redirect(new MockHttpServletRequest(), response, new Collection("test", Mockito.mock(Config.class)), ClickLog.Type.CLICK, 0, null, URI.create("http://redirected.com/"), null, "token", false, null, null);
            Assert.fail("Exception in token processing should have bubbled up");
        } catch (IllegalStateException e) {
            // Ensure the exception is not thrown by something else
            Assert.assertEquals("unit-test", e.getMessage());
        }
        
        Assert.assertNull(response.getRedirectedUrl());
    }
    
    @Test
    public void testClickLogWithoutReferer() throws Exception {
        LogService logService = mock(LogService.class);
        
        ClickController controller = clickController(logService);
        
        controller.redirect(new MockHttpServletRequest(), 
            new MockHttpServletResponse(), 
            new Collection("test", Mockito.mock(Config.class)), 
            ClickLog.Type.CLICK, 
            0, 
            new ProfileId("myprofile"), 
            URI.create("http://redirected.com/"), 
            URI.create("http://foo/"), 
            "token", 
            false, 
            null, 
            "da+ &query");
        ArgumentCaptor<ClickLog> clickLogAc = ArgumentCaptor.forClass(ClickLog.class);
        verify(logService, times(1)).logClick(clickLogAc.capture());
        
        ClickLog clickLog = clickLogAc.getValue();
        
        Assert.assertEquals("http://fake/?collection=test&profile=myprofile&query=da%2B%20%26query", 
            clickLog.getReferer().toExternalForm());
        
        //check that it is also a valid URI
        new URI(clickLog.getReferer().toExternalForm());
    }
    
    @Test
    public void testClickLogWithReferer() throws Exception {
        LogService logService = mock(LogService.class);
        
        ClickController controller = clickController(logService);
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("referer", "http://foo.com/?query=da%2B%20%26query&collection=test&profile=myprofile");
        controller.redirect(request, 
            new MockHttpServletResponse(), 
            new Collection("test", Mockito.mock(Config.class)), 
            ClickLog.Type.CLICK, 
            0, 
            null, 
            URI.create("http://redirected.com/"), 
            URI.create("http://foo/"), 
            "token", 
            false, 
            null, 
            null);
        ArgumentCaptor<ClickLog> clickLogAc = ArgumentCaptor.forClass(ClickLog.class);
        verify(logService, times(1)).logClick(clickLogAc.capture());
        
        ClickLog clickLog = clickLogAc.getValue();
        
        Assert.assertEquals("http://fake/?collection=test&profile=myprofile&query=da%2B%20%26query", 
            clickLog.getReferer().toExternalForm());
        
        //check that it is also a valid URI
        new URI(clickLog.getReferer().toExternalForm());
    }
    
    @Test
    public void testSaveResultHistoryWithoutQuery() throws Exception {
        LogService logService = mock(LogService.class);
        
        IndexRepository indexRepository = mock(IndexRepository.class);
        Mockito.when(indexRepository.getResult(Mockito.anyObject(), Mockito.anyObject()))
            .thenReturn(new Result(1, 999, "title", "collection", 1, null, "http://example.com/", "summary", null, null, null, null, null,
                null, null, null, null, null, null, null, "http://example.com", null, false, false, false));
        
        ConfigRepository configRepository = mock(ConfigRepository.class);
        ServerConfigReadOnly srverConfig = mock(ServerConfigReadOnly.class);
        when(srverConfig.get(ServerKeys.SERVER_SECRET)).thenReturn("");
        when(configRepository.getServerConfig()).thenReturn(srverConfig);
        ServiceConfig serviceConfig = mock(ServiceConfig.class);
        when(serviceConfig.get(FrontEndKeys.ModernUI.Session.SearchHistory.METADATA_TO_RECORD)).thenReturn(new ArrayList<>());
        when(configRepository.getServiceConfig(Mockito.anyString(), Mockito.anyString())).thenReturn(serviceConfig);
        
        SearchHistoryRepository searchHistoryRepository = mock(SearchHistoryRepository.class);
        
        ClickController controller = clickController(logService);
        controller.setIndexRepository(indexRepository);
        controller.setConfigRepository(configRepository);
        controller.setSearchHistoryRepository(searchHistoryRepository);

        Collection mockCollection = new Collection("test", Mockito.mock(Config.class));
        Mockito.when(mockCollection.getConfiguration().valueAsBoolean(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(true);

        ArgumentCaptor<ClickHistory> captor = ArgumentCaptor.forClass(ClickHistory.class);
        
        controller.saveResultHistory(mockCollection, Optional.of("test-profile"), new SearchUser("userId"), URI.create("http://foo/"), Optional.empty());
        
        Mockito.verify(searchHistoryRepository).saveClick(captor.capture());
        
        ClickHistory submittedClickHistory = captor.getValue();
        
        // Prior to SUPPORT-2625 this got null, which Hibernate rejected due to the DB schema
        Assert.assertEquals("", submittedClickHistory.getQuery());
    }

    private ClickController clickController(LogService logService) {
        AuthTokenManager authTokenManager = mock(AuthTokenManager.class);
        ConfigRepository configRepository = mock(ConfigRepository.class);
        ServerConfigReadOnly srverConfig = mock(ServerConfigReadOnly.class);
        when(srverConfig.get(ServerKeys.SERVER_SECRET)).thenReturn("");
        when(configRepository.getServerConfig()).thenReturn(srverConfig);
        
        
        when(authTokenManager.checkToken(anyString(), anyString(), anyString())).thenReturn(true);
        
        
        ClickController controller = new ClickController() {
            @Override
            protected String getRequestId(HttpServletRequest request, Collection collection) {
                return "dummy";
            }
        };
        
        controller.setMetrics(new MetricRegistry());
        controller.setLogService(logService);
        controller.setConfigRepository(configRepository);
        controller.setAuthTokenManager(authTokenManager);
        
        return controller;
    }
    
}
