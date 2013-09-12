package com.funnelback.publicui.test.search.lifecycle.input.processors;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.ExploreQuery;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.test.mock.MockExploreQueryGenerator;

public class ExploreQueryTests {

    private ExploreQuery processor;
    
    @Before
    public void before() {
        processor = new ExploreQuery();
        processor.setGenerator(new MockExploreQueryGenerator());
    }
    
    @Test
    public void testMissingData() throws InputProcessorException {
        // No transaction
        processor.processInput(null);
        
        // No question
        processor.processInput(new SearchTransaction(null, null));
        
        // No collection
        SearchQuestion question = new SearchQuestion();
        SearchTransaction st = new SearchTransaction(question, null);
        processor.processInput(st);
        Assert.assertNull(st.getQuestion().getQuery());
        
        // No query
        question.setCollection(new Collection("dummy", null));
        processor.processInput(new SearchTransaction(question, null));
        Assert.assertNull(st.getQuestion().getQuery());

        // No explore query
        question.setQuery("explore abc website");
        processor.processInput(new SearchTransaction(question, null));
        Assert.assertEquals("explore abc website", st.getQuestion().getQuery());
    }    

    @Test
    public void test() throws InputProcessorException {
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);
        st.getQuestion().setQuery("explore:http://host.com/url.html and another term");
        st.getQuestion().setCollection(new Collection("dummy", null));
        
        processor.processInput(st);
        Assert.assertEquals("null queries for http://host.com/url.html on collection dummy and another term", st.getQuestion().getQuery());
        
        st.getQuestion().setQuery("explore:http://host.com/url.html and another term");
        st.getQuestion().getRawInputParameters().put("exp", new String[] {"42"});
        processor.processInput(st);
        Assert.assertEquals("42 queries for http://host.com/url.html on collection dummy and another term", st.getQuestion().getQuery());
        
        st.getQuestion().setQuery("explore:http://host.com/url.html and another term");
        st.getQuestion().getRawInputParameters().put("exp", new String[] {"bad"});
        processor.processInput(st);
        Assert.assertEquals("null queries for http://host.com/url.html on collection dummy and another term", st.getQuestion().getQuery());
    }
    
    @Test
    public void testExploreUrlIsRemoved() throws Exception {
    	SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);
        st.getQuestion().setQuery("explore:http://host.com/url.html and another term");
        st.getQuestion().setCollection(new Collection("dummy", null));
        
        processor.processInput(st);
        Assert.assertEquals("null queries for http://host.com/url.html on collection dummy and another term", st.getQuestion().getQuery());
        
        Assert.assertEquals("http://host.com/url.html", ((String[]) st.getQuestion().getAdditionalParameters().get(RequestParameters.REMOVE_URLS))[0]);
    }
    
}
