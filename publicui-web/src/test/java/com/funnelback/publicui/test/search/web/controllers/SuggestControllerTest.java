package com.funnelback.publicui.test.search.web.controllers;

import static com.funnelback.config.keys.Keys.FrontEndKeys;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletResponse;

import com.funnelback.common.profile.ProfileNotFoundException;
import com.funnelback.config.configtypes.service.ServiceConfig;
import com.funnelback.publicui.search.service.ConfigRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.ProfileId;
import com.funnelback.dataapi.connector.padre.suggest.Suggestion;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.ExecutionContext;
import com.funnelback.publicui.search.service.Suggester;
import com.funnelback.publicui.search.web.controllers.SuggestController;
import com.funnelback.publicui.utils.JsonPCallbackParam;
import com.funnelback.publicui.utils.web.ExecutionContextHolder;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class SuggestControllerTest {

    private SuggestController suggestController;
    
    @Before
    public void before() throws ProfileNotFoundException {
        Suggester suggester = mock(Suggester.class);
        when(suggester.suggest(any(), any(), any(), anyInt(), any(), anyDouble(), any())).thenReturn(new ArrayList<Suggestion>());
        
        ExecutionContextHolder holder = mock(ExecutionContextHolder.class);
        when(holder.getExecutionContext()).thenReturn(ExecutionContext.Unknown);

        ConfigRepository configRepository = mock(ConfigRepository.class);
        ServiceConfig serviceConfig = mock(ServiceConfig.class);
        when(configRepository.getServiceConfig(anyString(),anyString())).thenReturn(serviceConfig);
        when(serviceConfig.get(FrontEndKeys.ModernUi.Session.SESSION)).thenReturn(true);
        when(serviceConfig.get(FrontEndKeys.ModernUi.Session.SearchHistory.Suggest.SUGGEST)).thenReturn(true);
        when(serviceConfig.get(FrontEndKeys.ModernUi.Session.SearchHistory.SIZE)).thenReturn(10);
        when(serviceConfig.get(FrontEndKeys.ModernUi.Session.SearchHistory.Suggest.DISPLAY_TEMPLATE))
            .thenReturn("");

        suggestController = new SuggestController();
        suggestController.setConfigRepository(configRepository);
        suggestController.setSuggester(suggester);
        suggestController.setExecutionContextHolder(holder);
    }
    
    @Test
    public void testNoCollection() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        suggestController.noCollection(response);
        Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }
    
    @Test
    public void testInvalidCollection() throws IOException {
        MockHttpServletResponse response = new MockHttpServletResponse();
        suggestController.suggestJava(null,
                new ProfileId(DefaultValues.DEFAULT_PROFILE),
                "ab", 0, 0, "json", 0.5, "abc", new JsonPCallbackParam("cb"), null, new MockHttpServletRequest(), response);
        
        Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
    }
    
    /**
     * @see SUPPORT-2099
     * @throws IOException
     */
    @Test
    public void testJsonp() throws IOException {
        Config config = mock(Config.class);
        when(config.value(any())).thenReturn("");

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        suggestController.suggestJava(new Collection("test", config), new ProfileId("profile"), "query", 10, 0, "json", 0, null, null, null, request, response);
        
        Assert.assertNull("No content type should be set by default (defaults to the one configured for the view)", response.getContentType());
        
        response = new MockHttpServletResponse();
        suggestController.suggestJava(new Collection("test", config), new ProfileId("profile"), "query", 10, 0, "json", 0, null, new JsonPCallbackParam("callback"), null, request, response);
        Assert.assertEquals("Content type should have been set as 'callback' was used (JSONP)", "application/javascript", response.getContentType());

    }
    
}
