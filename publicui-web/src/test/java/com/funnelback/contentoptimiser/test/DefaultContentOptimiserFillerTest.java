package com.funnelback.contentoptimiser.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
    public void testFillHints() throws XmlParsingException, IOException {
        ContentOptimiserFiller f = new DefaultContentOptimiserFiller();

        ContentOptimiserModel comparison = 
            callSetImportantUrlWith(
                getDefaultTestResultPacket(), 
                "http://test-data.funnelback.com/Shakespeare/lear/lear.4.7.html",
                f);
        
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
        ContentOptimiserModel comparison = 
            callSetImportantUrlWith(
                getDefaultTestResultPacket(), 
                "http://test-data.funnelback.com/Shakespeare/lear/lear.4.7.html");

        assertNotNull(comparison.getSelectedDocument());
        assertEquals(new Integer(3),comparison.getSelectedDocument().getRank());
        assertEquals(10.004,comparison.getHintsByName().get("content").getWin(),0.0001);
        assertEquals(0,comparison.getHintsByName().get("offlink").getWin(),0.0001);
        assertEquals(1.755,comparison.getHintsByName().get("urllen").getWin(),0.0001);
    }
    
    @Test
    public void testSetImportantURLalreadyThereInDisplayUrlField() throws XmlParsingException, IOException {
        ResultPacket rp = getDefaultTestResultPacket();
        final String testUrl = "http://test.url";
        final int testStartResultIndex = rp.getResults().size() / 2;
        
        // Set few display URLs to be the same as the test URL
        rp.getResults().get(testStartResultIndex).setDisplayUrl(testUrl);
        final Integer expectedSelectedDocumentRank = rp.getResults().get(testStartResultIndex).getRank();

        rp.getResults().get(testStartResultIndex).setDisplayUrl(testUrl);
        
        assertEquals("When there is more than one document whose displayUrl is matched with the submitted URL, "
            + "it should return the first one", 
            expectedSelectedDocumentRank, 
            callSetImportantUrlWith(rp, testUrl)
                .getSelectedDocument()
                .getRank());
    }
    
    @Test
    public void testSetImportantURLalreadyThereInDisplayUrlAndLiveUrlFields() throws XmlParsingException, IOException {
        ResultPacket rp = getDefaultTestResultPacket();
        final String testUrl = "http://test.url";
        final int testStartResultIndex = rp.getResults().size() / 2;
        
        // Set few display URLs to be the same as the test URL
        rp.getResults().get(testStartResultIndex).setDisplayUrl(testUrl);
        rp.getResults().get(testStartResultIndex).setDisplayUrl(testUrl);
        
        // Set few live URLs to be the same as the test URL
        rp.getResults().get(testStartResultIndex + 2).setLiveUrl(testUrl);
        final Integer expectedSelectedDocumentRank = rp.getResults().get(testStartResultIndex + 2).getRank();

        rp.getResults().get(testStartResultIndex + 3).setLiveUrl(testUrl);
        
        assertEquals("When there is more than one document whose displayUrl and liveUrl is matched with the submitted URL, "
            + "it should return the first one with correct live URL", 
            expectedSelectedDocumentRank, 
            callSetImportantUrlWith(rp, testUrl)
                .getSelectedDocument()
                .getRank());
    }
    
    @Test
    public void testSetImportantURLalreadyThereInAllUrlFields() throws XmlParsingException, IOException {
        ResultPacket rp = getDefaultTestResultPacket();
        final String testUrl = "http://test.url";
        final int testStartResultIndex = rp.getResults().size() / 2;
        
        // Set few display URLs to be the same as the test URL
        rp.getResults().get(testStartResultIndex).setDisplayUrl(testUrl);
        rp.getResults().get(testStartResultIndex).setDisplayUrl(testUrl);
        
        // Set few live URLs to be the same as the test URL
        rp.getResults().get(testStartResultIndex + 2).setLiveUrl(testUrl);
        rp.getResults().get(testStartResultIndex + 3).setLiveUrl(testUrl);
        
        // Test compare indexUrl field
        rp.getResults().get(testStartResultIndex + 4).setIndexUrl(testUrl);
        final Integer expectedSelectedDocumentRank = rp.getResults().get(testStartResultIndex + 4).getRank();

        rp.getResults().get(testStartResultIndex + 5).setIndexUrl(testUrl);
        
        assertEquals("When there is more than one document whose displayUrl, liveUrl and indexUrl is matched with the submitted URL, "
            + "it should return the first one with correct index URL", 
            expectedSelectedDocumentRank, 
            callSetImportantUrlWith(rp, testUrl)
                .getSelectedDocument()
                .getRank());
    }
    
    @Test
    public void testSetImportantURLnotThereYet() throws XmlParsingException, IOException {
        ContentOptimiserModel comparison = 
            callSetImportantUrlWith(
                getDefaultTestResultPacket(), 
                "http://test-data.funnelback.com/Shakespeare/lear/lear.5.1.html");
        
        assertNotNull(comparison.getSelectedDocument());

        assertEquals(new Integer(18),comparison.getSelectedDocument().getRank());
        assertEquals(30.504,comparison.getHintsByName().get("content").getWin(),0.0001);
        assertEquals(0,comparison.getHintsByName().get("offlink").getWin(),0.0001);
        assertEquals(1.755,comparison.getHintsByName().get("urllen").getWin(),0.0001);
    }

    @Test
    public void testConsumeResultPacket() throws XmlParsingException, IOException {
        ResultPacket rp = getDefaultTestResultPacket();
        
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
    
    private ResultPacket getDefaultTestResultPacket() throws IOException, XmlParsingException {
        StaxStreamParser parser = new StaxStreamParser();
        ResultPacket rp = parser.parse(
            FileUtils.readFileToByteArray(new File("src/test/resources/padre-xml/explain-mockup-v2.xml")),
            StandardCharsets.UTF_8,
            false);
        
        return rp;
    }
    
    private ContentOptimiserModel callSetImportantUrlWith(ResultPacket resultPacket, String testUrl) {
        return callSetImportantUrlWith(resultPacket, testUrl, null);
    }
    
    private ContentOptimiserModel callSetImportantUrlWith(ResultPacket resultPacket, String testUrl, ContentOptimiserFiller f) {
        if (f == null) {
            f = new DefaultContentOptimiserFiller();
        }
        
        ContentOptimiserModel comparison = new ContentOptimiserModel();
        RankingFeatureFactory hf = new DefaultRankingFeatureFactory();

        f.consumeResultPacket(comparison, resultPacket, hf);
        assertNull(comparison.getSelectedDocument());
        
        SearchResponse response = new SearchResponse();
        response.setResultPacket(resultPacket);
        SearchQuestion question = new SearchQuestion();
        question.getRawInputParameters().put(RequestParameters.CONTENT_OPTIMISER_URL, new String[] {testUrl});
        SearchTransaction allTransaction = new SearchTransaction(question,response);
        
        f.setImportantUrl(comparison, allTransaction);

        return comparison;
    }

}
