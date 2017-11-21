package com.funnelback.publicui.test.search.lifecycle.output.processors;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.lifecycle.output.processors.QuickLinks;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.test.search.lifecycle.data.fetchers.padre.xml.impl.StaxStreamTestHelper;
import com.funnelback.publicui.xml.XmlParsingException;

public class QuickLinksTests {

    private QuickLinks processor;
    
    private SearchTransaction st;

    
    @Before
    public void before() throws XmlParsingException, IOException {
        SearchQuestion question = new SearchQuestion();
        question.setQuery("quicklinks");
        question.setCollection(new Collection("quicklinks", null));
        
        SearchResponse response = new SearchResponse();
        response.setResultPacket(StaxStreamTestHelper.parse(new File("src/test/resources/padre-xml/quicklinks.xml")));
        
        st = new SearchTransaction(question, response);
        processor = new QuickLinks();
    }
    
    @Test
    public void test() throws OutputProcessorException {
        processor.processOutput(st);
        
        Result r = st.getResponse().getResultPacket().getResults().get(0);
        com.funnelback.publicui.search.model.padre.QuickLinks qls = r.getQuickLinks();
        
        Assert.assertEquals("australia.gov.au/", qls.getDomain());
        
        Assert.assertEquals("http://australia.gov.au/services", qls.getQuickLinks().get(0).getUrl());
        Assert.assertEquals("http://australia.gov.au/people", qls.getQuickLinks().get(1).getUrl());
        Assert.assertEquals("http://australia.gov.au/topics", qls.getQuickLinks().get(2).getUrl());
        Assert.assertEquals("http://australia.gov.au/life-events", qls.getQuickLinks().get(3).getUrl());
        Assert.assertEquals("http://australia.gov.au/directories", qls.getQuickLinks().get(4).getUrl());
        Assert.assertEquals("http://australia.gov.au/publications", qls.getQuickLinks().get(5).getUrl());
        Assert.assertEquals("http://australia.gov.au/news-and-media", qls.getQuickLinks().get(6).getUrl());
        Assert.assertEquals("http://australia.gov.au/service/electoral-enrolment", qls.getQuickLinks().get(7).getUrl());
        Assert.assertEquals("http://australia.gov.au/faqs/government-services-faqs/immigration-and-visas", qls.getQuickLinks().get(8).getUrl());
        Assert.assertEquals("http://australia.gov.au/topics/australian-facts-and-figures/public-holidays", qls.getQuickLinks().get(9).getUrl());

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
    }
}
