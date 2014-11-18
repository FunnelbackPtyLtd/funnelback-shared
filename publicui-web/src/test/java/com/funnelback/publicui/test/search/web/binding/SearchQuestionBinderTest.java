package com.funnelback.publicui.test.search.web.binding;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
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
        from.setRequestId("user-id");
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
    
}
