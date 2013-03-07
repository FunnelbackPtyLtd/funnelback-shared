package com.funnelback.publicui.test.search.lifecycle.output.processors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.lifecycle.output.processors.QueryCleanOutputProcessor;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class QueryCleanTests {

    private SearchTransaction st;
    private QueryCleanOutputProcessor processor;

    @Before
    public void before() {
        processor = new QueryCleanOutputProcessor();
        
        SearchResponse response = new SearchResponse();
        response.setResultPacket(new ResultPacket());
        st = new SearchTransaction(new SearchQuestion(), response);
    }
    
    @Test
    public void testMissingData() throws Exception{
        // No transaction
        processor.processOutput(null);
        
        // No response & question
        processor.processOutput(new SearchTransaction(null, null));
        
        // No question
        processor.processOutput(new SearchTransaction(null, new SearchResponse()));
        
        // No response
        processor.processOutput(new SearchTransaction(new SearchQuestion(), null));
        
        // No results
        SearchResponse response = new SearchResponse();
        processor.processOutput(new SearchTransaction(null, response));
        
        // No results in packet
        response.setResultPacket(new ResultPacket());
        processor.processOutput(new SearchTransaction(null, response));
        
        // No query
        st.getResponse().getResultPacket().setQuery(null);
        processor.processOutput(st);
    }

    @Test
    public void testNothingToClean() throws OutputProcessorException {
        st.getResponse().getResultPacket().setQuery("this is a query");
        processor.processOutput(st);
        Assert.assertEquals("this is a query", st.getResponse().getResultPacket().getQueryCleaned());
    }
    
    @Test
    public void testWeightedOperators() throws OutputProcessorException {
        st.getResponse().getResultPacket().setQuery("this is a query^0.123 with weighted^0.456 operators");
        processor.processOutput(st);
        Assert.assertEquals("this is a query with weighted operators", st.getResponse().getResultPacket().getQueryCleaned());
    }
    
    @Test
    public void testFacetsConstraints() throws OutputProcessorException {
        st.getResponse().getResultPacket().setQuery("this query X:\"$++ abcd $++\" has v:\"folder/file\" facets Y:\"$++ ab cd ef $++\" constraints");
        st.getQuestion().getFacetsQueryConstraints().add("X:\"$++ abcd $++\"");
        st.getQuestion().getFacetsQueryConstraints().add("v:\"folder/file\"");
        st.getQuestion().getFacetsQueryConstraints().add("Y:\"$++ ab, cd & ef $++\"");
        processor.processOutput(st);
        Assert.assertEquals("this query has facets constraints", st.getResponse().getResultPacket().getQueryCleaned());
    }
    

}
