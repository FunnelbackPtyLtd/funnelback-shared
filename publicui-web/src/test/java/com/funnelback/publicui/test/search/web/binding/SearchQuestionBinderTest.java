package com.funnelback.publicui.test.search.web.binding;

import java.util.Locale;
import static org.mockito.Mockito.*;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.servlet.LocaleResolver;

import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.web.binding.SearchQuestionBinder;

public class SearchQuestionBinderTest {

    @Test
    public void testFromSearchQuestion() {
        SearchQuestion from = new SearchQuestion();
        from.getRawInputParameters().put("my-param", new String[] {"value1", "value2"});
        from.setQuery("query");
        from.setOriginalQuery("original query");
        from.setCollection(new Collection("coll", null));
        from.setProfile("profile");
        from.setImpersonated(true);
        from.setRequestIdToLog("user-id");
        from.setLocale(Locale.JAPANESE);
        from.setCnClickedCluster("cluster");
        from.getCnPreviousClusters().add("previous-clusters");
        from.setClive(new String[] {"ab", "cd"});
        
        SearchQuestion to = new SearchQuestion();
        SearchQuestionBinder.bind(from, to);
        
        Assert.assertArrayEquals(new String[] {"value1", "value2"}, to.getRawInputParameters().get("my-param"));
        Assert.assertEquals("query", to.getQuery());
        Assert.assertEquals("original query", to.getOriginalQuery());
        Assert.assertEquals("coll", to.getCollection().getId());
        Assert.assertEquals("profile", to.getProfile());
        Assert.assertEquals(true, to.isImpersonated());
        Assert.assertEquals("user-id", to.getRequestIdToLog());
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
    public void testGetIpRemoteAddr() {
        LocaleResolver loc = mock(LocaleResolver.class);
        SearchQuestion question = new SearchQuestion();
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRemoteAddr()).thenReturn("1.2.3.4");
        question.setCollection(new Collection("dummy", new NoOptionsConfig("dummy")));

        SearchQuestionBinder.bind(req, question, loc);        
        Assert.assertEquals("1.2.3.4", SearchQuestionBinder.getRequestIp(question));
    }
    
    @Test
    public void testGetIpForwardedFor() {
        LocaleResolver loc = mock(LocaleResolver.class);
        SearchQuestion question = new SearchQuestion();
        question.setCollection(new Collection("dummy", new NoOptionsConfig("dummy")));

        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRemoteAddr()).thenReturn("1.2.3.4");
        when(req.getHeader("X-Forwarded-For")).thenReturn("5.6.7.8");

        SearchQuestionBinder.bind(req, question, loc);        
        Assert.assertEquals("5.6.7.8", SearchQuestionBinder.getRequestIp(question));

    }
    
    @Test
    public void testGetIpForwardedForMultiple() {
        LocaleResolver loc = mock(LocaleResolver.class);
        SearchQuestion question = new SearchQuestion();
        question.setCollection(new Collection("dummy", new NoOptionsConfig("dummy")));
        
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRemoteAddr()).thenReturn("1.2.3.4");
        when(req.getHeader("X-Forwarded-For")).thenReturn("5.6.7.8, 9.10.11.12, 13.14.15.16");

        SearchQuestionBinder.bind(req, question, loc);        
        Assert.assertEquals("5.6.7.8", SearchQuestionBinder.getRequestIp(question));

    }

    @Test
    public void testGetIpForwardedForMultipleSpaces() {
        LocaleResolver loc = mock(LocaleResolver.class);
        SearchQuestion question = new SearchQuestion();
        question.setCollection(new Collection("dummy", new NoOptionsConfig("dummy")));
        
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRemoteAddr()).thenReturn("1.2.3.4");
        when(req.getHeader("X-Forwarded-For")).thenReturn(" 5.6.7.8 ,  9.10.11.12 , 13.14.15.16");

        SearchQuestionBinder.bind(req, question, loc);        
        Assert.assertEquals("5.6.7.8", SearchQuestionBinder.getRequestIp(question));

    }

}
