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
import org.junit.Test;

import com.funnelback.contentoptimiser.DefaultHintFactory;
import com.funnelback.contentoptimiser.DefaultUrlCauseFiller;
import com.funnelback.contentoptimiser.HintFactory;
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
		f.setImportantUrl(comparison,rp);
		

		assertNotNull(comparison.getHintsByName().get("offlink"));
		assertNotNull(comparison.getHintsByName().get("urllen"));
		assertNotNull(comparison.getHintsByName().get("content"));
		assertEquals(29,comparison.getHintsByWin().size());
		assertNotNull(comparison.getWeights().get("offlink"));
		
		f.fillHints(comparison);
		
		assertNotNull(comparison.getHintsByName().get("urllen"));
		assertNull(comparison.getHintsByName().get("content"));
		assertNull(comparison.getHintsByName().get("offlink"));
		assertEquals(1,comparison.getHintsByWin().size());
		assertNotNull(comparison.getWeights().get("offlink"));
	}

	
	@Test
	public void testConsumeResultPacket() throws XmlParsingException, IOException {
		StaxStreamParser parser = new StaxStreamParser();
		ResultPacket rp = parser.parse(FileUtils.readFileToString(new File("src/test/resources/padre-xml/explain-mockup.xml"), "UTF-8"));

		UrlCausesFiller f = new DefaultUrlCauseFiller();
		UrlComparison comparison = new UrlComparison();
		HintFactory hf = new DefaultHintFactory();

		f.consumeResultPacket(comparison, rp,hf);
		f.setImportantUrl(comparison,rp);

		
		Assert.assertNotNull(comparison.getImportantOne());
		Assert.assertEquals("1",comparison.getImportantOne().getRank());		
		

		assertEquals(40.877,comparison.getHintsByName().get("content").getScores().get("1"),0.0001);
		assertEquals(0,comparison.getHintsByName().get("offlink").getScores().get("1"),0.0001);
		assertEquals(7.168,comparison.getHintsByName().get("urllen").getScores().get("1"),0.0001);

		assertEquals(31.406,comparison.getHintsByName().get("content").getScores().get("2"),0.0001);
		assertEquals(0,comparison.getHintsByName().get("offlink").getScores().get("2"),0.0001);
		assertEquals(7.490,comparison.getHintsByName().get("urllen").getScores().get("2"),0.0001);
		
		
		assertNotNull(comparison.getWeights());
		assertEquals(41,comparison.getWeights().get("content"),0.0001);
		assertEquals(14,comparison.getWeights().get("offlink"),0.0001);
		assertEquals(14,comparison.getWeights().get("urllen"),0.0001);
	}
}
