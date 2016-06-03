package com.funnelback.contentoptimiser.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.funnelback.contentoptimiser.DefaultRankingFeatureFactory;
import com.funnelback.contentoptimiser.RankingFeatureFactory;
import com.funnelback.contentoptimiser.processors.ContentOptimiserFiller;
import com.funnelback.contentoptimiser.processors.impl.DefaultContentOptimiserFiller;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.ContentOptimiserModel;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.RankingFeatureMaxOther;
import com.funnelback.publicui.xml.XmlParsingException;
import com.funnelback.publicui.xml.padre.StaxStreamParser;

public class DefaultContentOptimiserFillerTest {

    @Test
    public void testFillHints () throws XmlParsingException, IOException {
        StaxStreamParser parser = new StaxStreamParser();
        ResultPacket rp = parser.parse(
            FileUtils.readFileToString(new File("src/test/resources/padre-xml/explain-mockup.xml"), "UTF-8"),
            false);
    
        ContentOptimiserFiller f = new DefaultContentOptimiserFiller();
        ContentOptimiserModel comparison = new ContentOptimiserModel();
        RankingFeatureFactory hf = new DefaultRankingFeatureFactory();

        SearchResponse response = new SearchResponse();
        response.setResultPacket(rp);
        SearchQuestion question = new SearchQuestion();
        question.getRawInputParameters().put(RequestParameters.CONTENT_OPTIMISER_URL, new String[] {"http://test-data.funnelback.com/Shakespeare/lear/lear.4.7.html"});        
        SearchTransaction allTransaction = new SearchTransaction(question,response);
        
        f.consumeResultPacket(comparison, rp,hf);
        f.setImportantUrl(comparison, allTransaction);
        
        assertNotNull(comparison.getHintsByName().get("offlink"));
        assertNotNull(comparison.getHintsByName().get("urllen"));
        assertNotNull(comparison.getHintsByName().get("content"));
        assertNotNull(comparison.getHintsByName().get("imp_phrase"));
        assertEquals(30,comparison.getHintsByWin().size());
        assertNotNull(comparison.getWeights().get("offlink"));
        
        f.fillHintCollections(comparison);
        
        assertNotNull(comparison.getHintsByName().get("urllen"));
        assertNotNull(comparison.getHintsByName().get("content"));
        assertNotNull(comparison.getHintsByName().get("offlink"));
        assertNull(comparison.getHintsByName().get("imp_phrase"));
        assertEquals(3,comparison.getHintsByWin().size());
        assertNotNull(comparison.getWeights().get("offlink"));
        
        assertEquals(3,comparison.getHintCollections().size());
        assertEquals("On-Page: Content",comparison.getHintCollections().get(0).getName());
        assertEquals("On-Page: URL",comparison.getHintCollections().get(1).getName());
        assertEquals("Off-Page: Links",comparison.getHintCollections().get(2).getName());
        
        assertEquals(10.004,comparison.getHintCollections().get(0).getWin(),0.0001);
        assertEquals(1.755,comparison.getHintCollections().get(1).getWin(),0.0001);
        assertEquals(0,comparison.getHintCollections().get(2).getWin(),0.0001);
    }
    
    @Test
    public void testSetImportantURLalreadyThere() throws XmlParsingException, IOException {
        StaxStreamParser parser = new StaxStreamParser();
        ResultPacket rp = parser.parse(
            FileUtils.readFileToString(new File("src/test/resources/padre-xml/explain-mockup.xml"), "UTF-8"),
            false);
        ContentOptimiserFiller f = new DefaultContentOptimiserFiller();
        ContentOptimiserModel comparison = new ContentOptimiserModel();
        RankingFeatureFactory hf = new DefaultRankingFeatureFactory();

        f.consumeResultPacket(comparison, rp,hf);
        assertNull(comparison.getSelectedDocument());
        
        SearchResponse response = new SearchResponse();
        response.setResultPacket(rp);
        SearchQuestion question = new SearchQuestion();
        question.getRawInputParameters().put(RequestParameters.CONTENT_OPTIMISER_URL, new String[] {"http://test-data.funnelback.com/Shakespeare/lear/lear.4.7.html"});        
        SearchTransaction allTransaction = new SearchTransaction(question,response);

        f.setImportantUrl(comparison, allTransaction);
        
        assertNotNull(comparison.getSelectedDocument());
        assertEquals(new Integer(3),comparison.getSelectedDocument().getRank());
        assertEquals(10.004,comparison.getHintsByName().get("content").getWin(),0.0001);
        assertEquals(0,comparison.getHintsByName().get("offlink").getWin(),0.0001);
        assertEquals(1.755,comparison.getHintsByName().get("urllen").getWin(),0.0001);
    }
    
    @Test
    public void testSetImportantURLalreadyThereInVariousUrlFields() throws XmlParsingException, IOException {
        StaxStreamParser parser = new StaxStreamParser();
        ResultPacket rp = parser.parse(
            FileUtils.readFileToString(new File("src/test/resources/padre-xml/explain-mockup.xml"), "UTF-8"),
            false);
        ContentOptimiserFiller f = new DefaultContentOptimiserFiller();
        ContentOptimiserModel comparison = new ContentOptimiserModel();
        RankingFeatureFactory hf = new DefaultRankingFeatureFactory();

        f.consumeResultPacket(comparison, rp,hf);
        assertNull(comparison.getSelectedDocument());
        
        final String testUrl = "http://test.url";
        final int testStartResultIndex = rp.getResults().size() / 2;
        
        // Test compare displayUrl field
        Integer expectedSelectedDocumentRank;
        rp.getResults().get(testStartResultIndex).setDisplayUrl(testUrl);
        expectedSelectedDocumentRank = rp.getResults().get(testStartResultIndex).getRank();

        rp.getResults().get(testStartResultIndex).setDisplayUrl(testUrl);
        
        SearchResponse response = new SearchResponse();
        response.setResultPacket(rp);
        SearchQuestion question = new SearchQuestion();
        question.getRawInputParameters().put(RequestParameters.CONTENT_OPTIMISER_URL, new String[] {testUrl});        
        SearchTransaction allTransaction = new SearchTransaction(question,response);

        f.setImportantUrl(comparison, allTransaction);
        
        assertEquals("When there are more than one documents whose displayUrl are matched with the submitted URL, "
            + "it should return the first one", expectedSelectedDocumentRank, comparison.getSelectedDocument().getRank());
        
        // Test compare liveUrl field
        rp.getResults().get(testStartResultIndex + 2).setLiveUrl(testUrl);
        expectedSelectedDocumentRank = rp.getResults().get(testStartResultIndex + 2).getRank();

        rp.getResults().get(testStartResultIndex + 3).setLiveUrl(testUrl);

        allTransaction.getResponse().setResultPacket(rp);
        f.setImportantUrl(comparison, allTransaction);
        assertEquals("When there are more than one documents whose displayUrl and liveUrl are matched with the submitted URL, "
            + "it should return the first one with correct live URL", expectedSelectedDocumentRank, comparison.getSelectedDocument().getRank());
        
        // Test compare indexUrl field
        rp.getResults().get(testStartResultIndex + 4).setIndexUrl(testUrl);
        expectedSelectedDocumentRank = rp.getResults().get(testStartResultIndex + 4).getRank();

        rp.getResults().get(testStartResultIndex + 5).setIndexUrl(testUrl);

        allTransaction.getResponse().setResultPacket(rp);
        f.setImportantUrl(comparison, allTransaction);
        assertEquals("When there are more than one documents whose displayUrl, liveUrl and indexUrl are matched with the submitted URL, "
            + "it should return the first one with correct index URL", expectedSelectedDocumentRank, comparison.getSelectedDocument().getRank());
    }
    
    @Test
    public void testSetImportantURLnotThereYet() throws XmlParsingException, IOException {
        StaxStreamParser parser = new StaxStreamParser();
        ResultPacket rp = parser.parse(
            FileUtils.readFileToString(new File("src/test/resources/padre-xml/explain-mockup.xml"), "UTF-8"),
            false);
        ContentOptimiserFiller f = new DefaultContentOptimiserFiller();
        ContentOptimiserModel comparison = new ContentOptimiserModel();
        RankingFeatureFactory hf = new DefaultRankingFeatureFactory();

        f.consumeResultPacket(comparison, rp,hf);
        assertNull(comparison.getSelectedDocument());
        
        SearchResponse response = new SearchResponse();
        response.setResultPacket(rp);
        SearchQuestion question = new SearchQuestion();
        question.getRawInputParameters().put(RequestParameters.CONTENT_OPTIMISER_URL, new String[] {"http://test-data.funnelback.com/Shakespeare/lear/lear.5.1.html"});        
        SearchTransaction allTransaction = new SearchTransaction(question,response);

        f.setImportantUrl(comparison, allTransaction);
        
        assertNotNull(comparison.getSelectedDocument());

        assertEquals(new Integer(18),comparison.getSelectedDocument().getRank());
        assertEquals(30.504,comparison.getHintsByName().get("content").getWin(),0.0001);
        assertEquals(0,comparison.getHintsByName().get("offlink").getWin(),0.0001);
        assertEquals(1.755,comparison.getHintsByName().get("urllen").getWin(),0.0001);
    }

    @Test
    public void testConsumeResultPacket() throws XmlParsingException, IOException {
        
        StaxStreamParser parser = new StaxStreamParser();
        ResultPacket rp = parser.parse(
            FileUtils.readFileToString(  new File("src/test/resources/padre-xml/explain-mockup.xml"), "UTF-8"),
            false);

        ContentOptimiserFiller f = new DefaultContentOptimiserFiller();
        ContentOptimiserModel comparison = new ContentOptimiserModel();
        RankingFeatureFactory hf = new DefaultRankingFeatureFactory();

        f.consumeResultPacket(comparison, rp,hf);

        assertEquals(40.877,comparison.getHintsByName().get("content").getScores().get("1"),0.0001);
        assertEquals(0,comparison.getHintsByName().get("offlink").getScores().get("1"),0.0001);
        assertEquals(23.04,comparison.getHintsByName().get("urllen").getScores().get("1"),0.0001);
        
        assertTrue(comparison.getHintsByName().get("content") instanceof RankingFeatureMaxOther);
        assertTrue(comparison.getHintsByName().get("offlink") instanceof RankingFeatureMaxOther);
        assertTrue(comparison.getHintsByName().get("urllen") instanceof RankingFeatureMaxOther);
        
        assertEquals(31.406,comparison.getHintsByName().get("content").getScores().get("2"),0.0001);
        assertEquals(0,comparison.getHintsByName().get("offlink").getScores().get("2"),0.0001);
        assertEquals(24.075,comparison.getHintsByName().get("urllen").getScores().get("2"),0.0001);

        assertNotNull(comparison.getWeights());
        assertEquals(41,comparison.getWeights().get("content"),0.0001);
        assertEquals(14,comparison.getWeights().get("offlink"),0.0001);
        assertEquals(45,comparison.getWeights().get("urllen"),0.0001);
    }
}
