package com.funnelback.publicui.test.search.lifecycle.output.processors;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.lifecycle.output.processors.FixCacheAndClickLinks;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.curator.data.UrlAdvert;
import com.funnelback.publicui.search.model.padre.BestBet;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.auth.DefaultAuthTokenManager;
import com.funnelback.publicui.xml.padre.StaxStreamParser;

import lombok.SneakyThrows;

public class FixCacheAndClickLinksTests {

    private static final String CURATOR_ADVERT_LINK = "http://www.link.com";
    private FixCacheAndClickLinks processor;
    
    @SneakyThrows
    private SearchTransaction getTestSearchTransaction() {
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
        response.getCurator().getExhibits()
            .add(new UrlAdvert("titleHtml", "http://www.display.com", CURATOR_ADVERT_LINK, "descriptionHtml", new HashMap<>(), "category"));
        
        return new SearchTransaction(question, response);
    };
    
    @Before
    public void before() throws Exception{
        processor = new FixCacheAndClickLinks();
        processor.setAuthTokenManager(new DefaultAuthTokenManager());
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
        SearchTransaction st = getTestSearchTransaction();
        st.getQuestion().getCollection().getConfiguration().setValue(Keys.CLICK_TRACKING, "false");
        processor.processOutput(st);

        for (Result r: st.getResponse().getResultPacket().getResults()) {
            Assert.assertTrue("".equals(r.getCacheUrl()) || r.getCacheUrl().startsWith("cache.cgi"));
            Assert.assertEquals(r.getClickTrackingUrl(), r.getLiveUrl());
        }
    }

    @Test
    public void testClickTracking() throws OutputProcessorException, UnsupportedEncodingException {
        SearchTransaction st = getTestSearchTransaction();
        processAndAssertClickTracking(st);
    }
    
    @Test
    public void testNoUserEnteredQuery() throws OutputProcessorException, UnsupportedEncodingException {
        SearchTransaction st = getTestSearchTransaction();
        st.getQuestion().setQuery(null);
        st.getQuestion().getMetaParameters().add("livelinks & pumpkins");
        processAndAssertClickTracking(st);
    }

    public void processAndAssertClickTracking(SearchTransaction st) throws OutputProcessorException, UnsupportedEncodingException {
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
        String bbTrackingUrl = bb.getClickTrackingUrl();
        Assert.assertTrue(bbTrackingUrl.contains("CLICK_LINK?"));
        Assert.assertTrue(bbTrackingUrl.contains("collection=" + st.getQuestion().getCollection().getId()));
        Assert.assertTrue(bbTrackingUrl.contains("url=" + URLEncoder.encode(bb.getLink(), "UTF-8")));
        Assert.assertTrue(bbTrackingUrl.contains("index_url=" + URLEncoder.encode(bb.getLink(), "UTF-8")));
        Assert.assertTrue(bbTrackingUrl.contains("type=FP"));
        Assert.assertTrue(bbTrackingUrl.contains("profile=" + st.getQuestion().getProfile()));

        UrlAdvert advert = (UrlAdvert) st.getResponse().getCurator().getExhibits().get(0);
        Assert.assertFalse(advert.getLinkUrl().contains("null"));
        String advertTrackingUrl = advert.getLinkUrl();
        Assert.assertTrue(advertTrackingUrl.contains("CLICK_LINK?"));
        Assert.assertTrue(advertTrackingUrl.contains("collection=" + st.getQuestion().getCollection().getId()));
        Assert.assertTrue(advertTrackingUrl.contains("url=" + URLEncoder.encode(CURATOR_ADVERT_LINK, "UTF-8")));
        Assert.assertTrue(advertTrackingUrl.contains("index_url=" + URLEncoder.encode(CURATOR_ADVERT_LINK, "UTF-8")));
        Assert.assertTrue(advertTrackingUrl.contains("type=FP"));
        Assert.assertTrue(advertTrackingUrl.contains("profile=" + st.getQuestion().getProfile()));
    }
    
}
