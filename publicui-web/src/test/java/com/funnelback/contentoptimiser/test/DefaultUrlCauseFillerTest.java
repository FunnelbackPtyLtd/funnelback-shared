package com.funnelback.contentoptimiser.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.funnelback.contentoptimiser.DefaultHintFactory;
import com.funnelback.contentoptimiser.DefaultUrlCauseFiller;
import com.funnelback.contentoptimiser.HintFactory;
import com.funnelback.contentoptimiser.HintMaxOther;
import com.funnelback.contentoptimiser.UrlCausesFiller;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.xml.impl.StaxStreamParser;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.RankingScore;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.UrlComparison;
import com.funnelback.publicui.xml.XmlParsingException;

public class DefaultUrlCauseFillerTest {

	@Test
	public void testFillHints () throws XmlParsingException, IOException {
		StaxStreamParser parser = new StaxStreamParser();
		ResultPacket rp = parser.parse(FileUtils.readFileToString(new File("src/test/resources/padre-xml/explain-mockup.xml"), "UTF-8"));
		UrlCausesFiller f = new DefaultUrlCauseFiller();
		UrlComparison comparison = new UrlComparison();
		HintFactory hf = new DefaultHintFactory();

		f.consumeResultPacket(comparison, rp,hf);
		String url = "http://test-data.funnelback.com/Shakespeare/lear/lear.4.7.html";
		f.setImportantUrl(comparison, rp, url);
		
		assertNotNull(comparison.getHintsByName().get("offlink"));
		assertNotNull(comparison.getHintsByName().get("urllen"));
		assertNotNull(comparison.getHintsByName().get("content"));
		assertNotNull(comparison.getHintsByName().get("imp_phrase"));
		assertEquals(29,comparison.getHintsByWin().size());
		assertNotNull(comparison.getWeights().get("offlink"));
		
		f.fillHintCollections(comparison);
		
		assertNotNull(comparison.getHintsByName().get("urllen"));
		assertNotNull(comparison.getHintsByName().get("content"));
		assertNotNull(comparison.getHintsByName().get("offlink"));
		assertNull(comparison.getHintsByName().get("imp_phrase"));
		assertEquals(3,comparison.getHintsByWin().size());
		assertNotNull(comparison.getWeights().get("offlink"));
		
		assertEquals(3,comparison.getHintCollections().size());
		assertEquals("content",comparison.getHintCollections().get(0).getName());
		assertEquals("URL",comparison.getHintCollections().get(1).getName());
		assertEquals("link based",comparison.getHintCollections().get(2).getName());
		
		assertEquals(10.004,comparison.getHintCollections().get(0).getWin(),0.0001);
		assertEquals(1.755,comparison.getHintCollections().get(1).getWin(),0.0001);
		assertEquals(0,comparison.getHintCollections().get(2).getWin(),0.0001);
	}
	
	@Test
	public void testSetImportantURLalreadyThere() throws XmlParsingException, IOException {
		StaxStreamParser parser = new StaxStreamParser();
		ResultPacket rp = parser.parse(FileUtils.readFileToString(new File("src/test/resources/padre-xml/explain-mockup.xml"), "UTF-8"));
		UrlCausesFiller f = new DefaultUrlCauseFiller();
		UrlComparison comparison = new UrlComparison();
		HintFactory hf = new DefaultHintFactory();

		f.consumeResultPacket(comparison, rp,hf);
		assertNull(comparison.getImportantOne());
		
		String url = "http://test-data.funnelback.com/Shakespeare/lear/lear.4.7.html";
		f.setImportantUrl(comparison, rp, url);
		
		assertNotNull(comparison.getImportantOne());
		assertEquals("3",comparison.getImportantOne().getRank());
		assertEquals(10.004,comparison.getHintsByName().get("content").getWin(),0.0001);
		assertEquals(0,comparison.getHintsByName().get("offlink").getWin(),0.0001);
		assertEquals(1.755,comparison.getHintsByName().get("urllen").getWin(),0.0001);
	}
	
	@Test
	public void testSetImportantURLnotThereYet() throws XmlParsingException, IOException {
		StaxStreamParser parser = new StaxStreamParser();
		ResultPacket rp = parser.parse(FileUtils.readFileToString(new File("src/test/resources/padre-xml/explain-mockup.xml"), "UTF-8"));
		UrlCausesFiller f = new DefaultUrlCauseFiller();
		UrlComparison comparison = new UrlComparison();
		HintFactory hf = new DefaultHintFactory();

		f.consumeResultPacket(comparison, rp,hf);
		assertNull(comparison.getImportantOne());
		
		String url = "http://test-data.funnelback.com/Shakespeare/lear/lear.5.1.html";
		f.setImportantUrl(comparison, rp, url);
		
		assertNotNull(comparison.getImportantOne());

		assertEquals("18",comparison.getImportantOne().getRank());
		assertEquals(30.504,comparison.getHintsByName().get("content").getWin(),0.0001);
		assertEquals(0,comparison.getHintsByName().get("offlink").getWin(),0.0001);
		assertEquals(1.755,comparison.getHintsByName().get("urllen").getWin(),0.0001);
	}

	@Test
	public void testConsumeResultPacket() throws XmlParsingException, IOException {
		
		StaxStreamParser parser = new StaxStreamParser();
		ResultPacket rp = parser.parse(FileUtils.readFileToString(new File("src/test/resources/padre-xml/explain-mockup.xml"), "UTF-8"));

		UrlCausesFiller f = new DefaultUrlCauseFiller();
		UrlComparison comparison = new UrlComparison();
		HintFactory hf = new DefaultHintFactory();

		f.consumeResultPacket(comparison, rp,hf);

		assertEquals(40.877,comparison.getHintsByName().get("content").getScores().get("1"),0.0001);
		assertEquals(0,comparison.getHintsByName().get("offlink").getScores().get("1"),0.0001);
		assertEquals(23.04,comparison.getHintsByName().get("urllen").getScores().get("1"),0.0001);
		
		assertTrue(comparison.getHintsByName().get("content") instanceof HintMaxOther);
		assertTrue(comparison.getHintsByName().get("offlink") instanceof HintMaxOther);
		assertTrue(comparison.getHintsByName().get("urllen") instanceof HintMaxOther);
		
		assertEquals(31.406,comparison.getHintsByName().get("content").getScores().get("2"),0.0001);
		assertEquals(0,comparison.getHintsByName().get("offlink").getScores().get("2"),0.0001);
		assertEquals(24.075,comparison.getHintsByName().get("urllen").getScores().get("2"),0.0001);

		assertNotNull(comparison.getWeights());
		assertEquals(41,comparison.getWeights().get("content"),0.0001);
		assertEquals(14,comparison.getWeights().get("offlink"),0.0001);
		assertEquals(45,comparison.getWeights().get("urllen"),0.0001);
	}
}
