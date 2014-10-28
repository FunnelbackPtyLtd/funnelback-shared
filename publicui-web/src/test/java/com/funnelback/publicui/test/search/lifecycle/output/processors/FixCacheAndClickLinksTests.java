package com.funnelback.publicui.test.search.lifecycle.output.processors;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.lifecycle.output.processors.FixCacheAndClickLinks;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.padre.BestBet;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.auth.DefaultAuthTokenManager;
import com.funnelback.publicui.xml.padre.StaxStreamParser;

public class FixCacheAndClickLinksTests {

    private FixCacheAndClickLinks processor;
    
    private SearchTransaction st;
    
    @Before
    public void before() throws Exception{
        processor = new FixCacheAndClickLinks();
        processor.setAuthTokenManager(new DefaultAuthTokenManager());
        
        SearchQuestion question = new SearchQuestion();
        question.setQuery("livelinks & pumpkins ");
        question.setCollection(new Collection("dummy", new NoOptionsConfig("dummy")));
        question.setProfile("profile-test");
        question.getRawInputParameters().put("HTTP_REFERER", new String[] {"REFERER"});
        
        SearchResponse response = new SearchResponse();
        response.setResultPacket(new StaxStreamParser().parse(
            FileUtils.readFileToString(new File("src/test/resources/padre-xml/fix-pseudo-live-links.xml")),
            false));
        response.getResultPacket().getBestBets().add(
                new BestBet("trigger", "http://www.url.com", "title", "description", "http://www.url.com"));
        
        st = new SearchTransaction(question, response);

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
    
    @Test
    public void testNoClickTracking() throws OutputProcessorException {
        st.getQuestion().getCollection().getConfiguration().setValue(Keys.CLICK_TRACKING, "false");
        processor.processOutput(st);

        for (Result r: st.getResponse().getResultPacket().getResults()) {
            Assert.assertTrue("".equals(r.getCacheUrl()) || r.getCacheUrl().startsWith("cache.cgi"));
            Assert.assertEquals(r.getClickTrackingUrl(), r.getLiveUrl());
        }
    }
    
    @Test
    public void testClickTracking() throws OutputProcessorException, UnsupportedEncodingException {
        st.getQuestion().getCollection().getConfiguration().setValue(Keys.CLICK_TRACKING, "true");
        st.getQuestion().getCollection().getConfiguration().setValue(Keys.ModernUI.CLICK_LINK, "CLICK_LINK");
        st.getQuestion().getCollection().getConfiguration().setValue(Keys.SERVER_SECRET, "plop");
        processor.processOutput(st);
        
        for (Result r: st.getResponse().getResultPacket().getResults()) {
            Assert.assertFalse(r.getCacheUrl().contains("null"));
            Assert.assertFalse(r.getClickTrackingUrl().contains("null"));
            Assert.assertTrue("".equals(r.getCacheUrl()) || r.getCacheUrl().startsWith("cache.cgi"));
            String trackingUrl = r.getClickTrackingUrl();
            Assert.assertTrue(trackingUrl.contains("CLICK_LINK?"));
            
            Assert.assertTrue(trackingUrl.contains("rank=" + r.getRank()));
            Assert.assertTrue(trackingUrl.contains("collection=" + st.getQuestion().getCollection().getId()));
            Assert.assertTrue(trackingUrl.contains("url=" + URLEncoder.encode(r.getLiveUrl(), "UTF-8")));
            Assert.assertTrue(trackingUrl.contains("index_url=" + URLEncoder.encode(r.getLiveUrl(), "UTF-8")));
            Assert.assertTrue(URLDecoder.decode(trackingUrl, "UTF-8").matches(".*auth=[a-zA-Z0-9+/]{22}.*"));
            Assert.assertTrue(trackingUrl.contains("query=livelinks+%26+pumpkins&"));
            Assert.assertTrue(trackingUrl.contains("profile=" + st.getQuestion().getProfile()));
            Assert.assertTrue(trackingUrl.contains("referer=REFERER"));
        }
        
        BestBet bb = st.getResponse().getResultPacket().getBestBets().get(0);
        Assert.assertFalse(bb.getClickTrackingUrl().contains("null"));
        String trackingUrl = bb.getClickTrackingUrl();
        Assert.assertTrue(trackingUrl.contains("CLICK_LINK?"));
        Assert.assertTrue(trackingUrl.contains("collection=" + st.getQuestion().getCollection().getId()));
        Assert.assertTrue(trackingUrl.contains("url=" + URLEncoder.encode(bb.getLink(), "UTF-8")));
        Assert.assertTrue(trackingUrl.contains("index_url=" + URLEncoder.encode(bb.getLink(), "UTF-8")));
        Assert.assertTrue(trackingUrl.contains("type=FP"));
        Assert.assertTrue(trackingUrl.contains("profile=" + st.getQuestion().getProfile()));

    }
    
    @Test
    public void testNoUserEnteredQuery() throws OutputProcessorException, UnsupportedEncodingException {
        st.getQuestion().setQuery(null);
        st.getQuestion().getMetaParameters().add("livelinks & pumpkins");
        testClickTracking();
    }
}
