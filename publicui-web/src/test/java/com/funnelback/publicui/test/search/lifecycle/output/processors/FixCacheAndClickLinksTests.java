package com.funnelback.publicui.test.search.lifecycle.output.processors;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.SneakyThrows;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.lifecycle.output.processors.FixCacheAndClickLinks;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.curator.Curator;
import com.funnelback.publicui.search.model.curator.data.UrlAdvert;
import com.funnelback.publicui.search.model.padre.BestBet;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.auth.DefaultAuthTokenManager;
import com.funnelback.publicui.utils.QueryStringUtils;
import com.funnelback.publicui.xml.padre.StaxStreamParser;
import com.google.common.collect.ImmutableList;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

public class FixCacheAndClickLinksTests {

    private static final String CURATOR_ADVERT_LINK = "http://www.link.com";
    private FixCacheAndClickLinks processor;
    
    private final File SEARCH_HOME = new File("src/test/resources/dummy-search_home");
    
    @SneakyThrows
    private SearchTransaction getTestSearchTransaction() {
        SearchQuestion question = new SearchQuestion();
        question.setQuery("livelinks & pumpkins ");
        question.setCollection(new Collection("dummy", new NoOptionsConfig(SEARCH_HOME, "dummy")));
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
        Curator curatorOrig = st.getResponse().getCurator();
        UrlAdvert urlAdvertOrig = (UrlAdvert) st.getResponse().getCurator().getExhibits().get(0);
        
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
            
            Map<String, String> qs = QueryStringUtils.toSingleMap(URI.create(trackingUrl).getRawQuery());
            
            Assert.assertEquals(Integer.toString(r.getRank()), qs.get("rank"));
            Assert.assertEquals(st.getQuestion().getCollection().getId(), qs.get("collection"));
            Assert.assertEquals(r.getLiveUrl(), qs.get("url"));
            Assert.assertEquals(r.getLiveUrl(), qs.get("index_url"));
            Assert.assertTrue(qs.get("auth").matches(".*[a-zA-Z0-9+/]{22}.*"));
            Assert.assertEquals("livelinks & pumpkins", qs.get("query"));
            Assert.assertEquals(st.getQuestion().getProfile(), qs.get("profile"));
            Assert.assertEquals("REFERER", qs.get("search_referer"));
        }
        
        BestBet bb = st.getResponse().getResultPacket().getBestBets().get(0);
        Assert.assertFalse(bb.getClickTrackingUrl().contains("null"));
        String bbTrackingUrl = bb.getClickTrackingUrl();
        Assert.assertTrue(bbTrackingUrl.contains("CLICK_LINK?"));
        
        Map<String, String> qs = QueryStringUtils.toSingleMap(URI.create(bbTrackingUrl).getRawQuery());
        
        Assert.assertEquals(st.getQuestion().getCollection().getId(), qs.get("collection"));
        Assert.assertEquals(bb.getLink(), qs.get("url"));
        Assert.assertEquals(bb.getLink(), qs.get("index_url"));
        Assert.assertEquals("FP", qs.get("type"));
        Assert.assertEquals(st.getQuestion().getProfile(), qs.get("profile"));

        UrlAdvert advert = (UrlAdvert) st.getResponse().getCurator().getExhibits().get(0);
        Assert.assertFalse(advert.getLinkUrl().contains("null"));
        String advertTrackingUrl = advert.getLinkUrl();
        Assert.assertTrue(advertTrackingUrl.contains("CLICK_LINK?"));
        
        Assert.assertFalse("The curator object may be shared between requests, we must create a new Curator instance"
            + " so that we may mess with its elements", curatorOrig == st.getResponse().getCurator());
        
        Assert.assertFalse("As UrlAdvert instances are shared between request we must create new UrlAdvert instances"
            + " rather than modifing the shared ones.", 
            urlAdvertOrig == st.getResponse().getCurator().getExhibits().get(0));
        qs = QueryStringUtils.toSingleMap(URI.create(advertTrackingUrl).getRawQuery());
        
        Assert.assertEquals(st.getQuestion().getCollection().getId(), qs.get("collection"));
        Assert.assertEquals(CURATOR_ADVERT_LINK, qs.get("url"));
        Assert.assertEquals(CURATOR_ADVERT_LINK, qs.get("index_url"));
        Assert.assertEquals("FP", qs.get("type"));
        Assert.assertEquals(st.getQuestion().getProfile(), qs.get("profile"));
    }
    
    @Test
    public void setClickTrackingUrlTestNotVisable() {
        SearchTransaction st = mock(SearchTransaction.class, RETURNS_DEEP_STUBS);
        
        when(st.getQuestion().getCollection().getConfiguration().valueAsBoolean(Keys.CLICK_TRACKING)).thenReturn(false);
        
        Result r1 = mock(Result.class);
        when(r1.isDocumentVisibleToUser()).thenReturn(false);
        
        Result r2 = mock(Result.class);
        when(r2.isDocumentVisibleToUser()).thenReturn(true);
        when(r2.getLiveUrl()).thenReturn("hey hey hey");
        
        List<Result> results = ImmutableList.<Result>builder().add(r1).add(r2).build();
        when(st.getResponse().getResultPacket().getResults()).thenReturn(results);
        
        FixCacheAndClickLinks fixCacheAndClickLinks = new FixCacheAndClickLinks();
        fixCacheAndClickLinks.setClickTrackingUrl(st, "bar");
        
        verify(r2, times(1)).setClickTrackingUrl("hey hey hey");
        
        verify(r1, times(1)).setClickTrackingUrl(null);
    }
    
}

