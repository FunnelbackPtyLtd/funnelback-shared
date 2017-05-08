package com.funnelback.publicui.test.search.web.binding;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.LocaleResolver;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.DefaultValues.RequestId;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.web.binding.SearchQuestionBinder;

public class SearchQuestionBinderTest {

    @Test
    public void testFromSearchQuestion() {
        Map<String, List<String>> queryStringMap = new HashMap<>();
        queryStringMap.put("my-other-param", Arrays.asList(new String[] {"v1", "v2"}));
        
        SearchQuestion from = new SearchQuestion();
        from.getRawInputParameters().put("my-param", new String[] {"value1", "value2"});
        from.setQueryStringMap(queryStringMap);
        from.setQuery("query");
        from.setOriginalQuery("original query");
        from.setCollection(new Collection("coll", null));
        from.setProfile("profile");
        from.setImpersonated(true);
        from.setRequestId("user-id");
        from.setLocale(Locale.JAPANESE);
        from.setCnClickedCluster("cluster");
        from.getCnPreviousClusters().add("previous-clusters");
        from.setClive(new String[] {"ab", "cd"});
        
        SearchQuestion to = new SearchQuestion();
        SearchQuestionBinder.bind(from, to);
        
        Assert.assertEquals(1, to.getRawInputParameters().size());
        Assert.assertEquals(1,  to.getQueryStringMapCopy().size());
        Assert.assertArrayEquals(new String[] {"value1", "value2"}, to.getRawInputParameters().get("my-param"));
        Assert.assertEquals(Arrays.asList(new String[] {"v1", "v2"}), to.getQueryStringMapCopy().get("my-other-param"));
        Assert.assertEquals("query", to.getQuery());
        Assert.assertEquals("original query", to.getOriginalQuery());
        Assert.assertEquals("coll", to.getCollection().getId());
        Assert.assertEquals("profile", to.getProfile());
        Assert.assertEquals(true, to.isImpersonated());
        Assert.assertEquals("user-id", to.getRequestId());
        Assert.assertEquals(Locale.JAPANESE, to.getLocale());
        Assert.assertEquals("cluster", to.getCnClickedCluster());
        Assert.assertEquals("previous-clusters", to.getCnPreviousClusters().get(0));
        Assert.assertArrayEquals(new String[] {"ab", "cd"}, to.getClive());
    }
    
    @Test
    public void testFromEmptyQuestion() {
        SearchQuestionBinder.bind(new SearchQuestion(), new SearchQuestion());
    }
    
    @Test
    public void testQueryStringMapCopyIsMutable() {
        Map<String, List<String>> queryStringMap = new HashMap<>();
        queryStringMap.put("my-other-param", Arrays.asList(new String[] {"v1", "v2"}));
        
        SearchQuestion from = new SearchQuestion();
        from.setQueryStringMap(queryStringMap);

        // New param
        from.getQueryStringMapCopy().put("new-param", Collections.emptyList());
        
        // Modify existing value
        from.getQueryStringMapCopy().get("my-other-param").add("v3");
    }
    
    @Test
    public void testFromRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("my-param", new String[] {"value1", "value2"});
        request.addParameter(RequestParameters.ContextualNavigation.CN_CLICKED, "cluster");
        request.addParameter(RequestParameters.ContextualNavigation.CN_PREV_PREFIX+"1", "cluster1");
        request.addParameter(RequestParameters.ContextualNavigation.CN_PREV_PREFIX+"2", "cluster2");
        request.addParameter("empty", "");
        request.addParameter("empty-array", new String[0]);
        request.addParameter("null", (String) null);
        
        request.setRequestURI("/request-uri");
        request.setAuthType("auth-type");
        request.addHeader("HOST", "http-host");
        request.setRemoteUser("remote-user");
        
        Config config = Mockito.mock(Config.class);
        Mockito.when(config.value(Mockito.eq(Keys.REQUEST_ID_TO_LOG), Mockito.anyString()))
            .thenReturn(RequestId.ip.toString());
        
        LocaleResolver localeResolver = Mockito.mock(LocaleResolver.class);
        Mockito.when(localeResolver.resolveLocale(Mockito.any()))
            .thenReturn(Locale.JAPANESE);
        
        SearchQuestion to = new SearchQuestion();
        to.setQuery("query");
        to.setCollection(new Collection("coll", config));
        
        SearchQuestionBinder.bind(request, to, localeResolver);

        Assert.assertEquals("127.0.0.1", to.getRequestId());
        
        Assert.assertEquals(7, to.getQueryStringMapCopy().size());
        Assert.assertEquals(Arrays.asList(new String[] {"cluster"}), to.getQueryStringMapCopy().get(RequestParameters.ContextualNavigation.CN_CLICKED));
        Assert.assertEquals(Arrays.asList(new String[] {"cluster1"}), to.getQueryStringMapCopy().get(RequestParameters.ContextualNavigation.CN_PREV_PREFIX+"1"));
        Assert.assertEquals(Arrays.asList(new String[] {"cluster2"}), to.getQueryStringMapCopy().get(RequestParameters.ContextualNavigation.CN_PREV_PREFIX+"2"));
        Assert.assertEquals(Arrays.asList(new String[] {"value1", "value2"}), to.getQueryStringMapCopy().get("my-param"));
        Assert.assertEquals(Arrays.asList(new String[] {""}), to.getQueryStringMapCopy().get("empty"));
        Assert.assertEquals(Collections.emptyList(), to.getQueryStringMapCopy().get("empty-array"));
        Assert.assertEquals(Arrays.asList(new String[] {null}), to.getQueryStringMapCopy().get("null"));

        Assert.assertEquals(13, to.getInputParameterMap().size());
        Assert.assertEquals("127.0.0.1", to.getInputParameterMap().get("REMOTE_ADDR"));
        Assert.assertEquals("/request-uri", to.getInputParameterMap().get("REQUEST_URI"));
        Assert.assertEquals("auth-type", to.getInputParameterMap().get("AUTH_TYPE"));
        Assert.assertEquals("http-host", to.getInputParameterMap().get("HTTP_HOST"));
        Assert.assertEquals("remote-user", to.getInputParameterMap().get("REMOTE_USER"));
        Assert.assertEquals("http://localhost/request-uri", to.getInputParameterMap().get("REQUEST_URL"));       
        Assert.assertArrayEquals(new String[] {"cluster"}, to.getRawInputParameters().get(RequestParameters.ContextualNavigation.CN_CLICKED));
        Assert.assertArrayEquals(new String[] {"cluster1"}, to.getRawInputParameters().get(RequestParameters.ContextualNavigation.CN_PREV_PREFIX+"1"));
        Assert.assertArrayEquals(new String[] {"cluster2"}, to.getRawInputParameters().get(RequestParameters.ContextualNavigation.CN_PREV_PREFIX+"2"));
        Assert.assertArrayEquals(new String[] {"value1", "value2"}, to.getRawInputParameters().get("my-param"));
        Assert.assertArrayEquals(new String[] {""}, to.getRawInputParameters().get("empty"));
        Assert.assertArrayEquals(new String[0], to.getRawInputParameters().get("empty-array"));
        Assert.assertArrayEquals(new String[] {null}, to.getRawInputParameters().get("null"));
        
        Assert.assertEquals("query", to.getQuery());
        Assert.assertEquals("query", to.getOriginalQuery());
        Assert.assertEquals("coll", to.getCollection().getId());
        Assert.assertEquals("_default", to.getProfile());
        Assert.assertEquals(false, to.isImpersonated());
        
        Assert.assertEquals(Locale.JAPANESE, to.getLocale());
        Assert.assertEquals("cluster", to.getCnClickedCluster());
        Assert.assertEquals("cluster1", to.getCnPreviousClusters().get(0));
        Assert.assertEquals("cluster2", to.getCnPreviousClusters().get(1));

    }
        
}

