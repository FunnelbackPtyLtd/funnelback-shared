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

import com.funnelback.contentoptimiser.DefaultUrlCauseFiller;
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
		
		f.consumeResultPacket(comparison, rp);
		f.setImportantUrl("", comparison, rp);
		
		List<RankingScore> causes = comparison.getUrls().get(0).getCauses();
		//assertEquals("offlink",causes.get(1).getName());
		assertTrue("offlink".equals(causes.get(1).getName()));
		assertNotNull(comparison.getWeights().get("offlink"));
		
		f.fillHints(comparison);

		
		assertFalse("offlink".equals(causes.get(1).getName()));
		assertNull(comparison.getWeights().get("offlink"));
		//Assert.assertEquals(4,comparison.getHints().size());
	}

	
	@Test
	public void testConsumeResultPacket() throws XmlParsingException, IOException {
		StaxStreamParser parser = new StaxStreamParser();
		ResultPacket rp = parser.parse(FileUtils.readFileToString(new File("src/test/resources/padre-xml/explain-mockup.xml"), "UTF-8"));

		UrlCausesFiller f = new DefaultUrlCauseFiller();
		UrlComparison comparison = new UrlComparison();

		f.consumeResultPacket(comparison, rp);
		f.setImportantUrl("", comparison, rp);

		
		Assert.assertNotNull(comparison.getImportantOne());
		Assert.assertEquals(4,comparison.getImportantOne().getRank());		
		
		List<RankingScore> causes = comparison.getUrls().get(0).getCauses();		
		assertNotNull(causes);
		assertEquals("content",causes.get(0).getName());
		assertEquals("offlink",causes.get(1).getName());
		assertEquals("urllen",causes.get(2).getName());
		assertEquals(40.877,causes.get(0).getPercentage(),0.0001);
		assertEquals(0,causes.get(1).getPercentage(),0.0001);
		assertEquals(7.168,causes.get(2).getPercentage(),0.0001);

		causes = comparison.getUrls().get(1).getCauses();		
		assertNotNull(causes);
		assertEquals("content",causes.get(0).getName());
		assertEquals("offlink",causes.get(1).getName());
		assertEquals("urllen",causes.get(2).getName());
		assertEquals(31.406,causes.get(0).getPercentage(),0.0001);
		assertEquals(0,causes.get(1).getPercentage(),0.0001);
		assertEquals(7.490,causes.get(2).getPercentage(),0.0001);

		assertNotNull(comparison.getWeights());
		assertEquals(41,comparison.getWeights().get("content"),0.0001);
		assertEquals(14,comparison.getWeights().get("offlink"),0.0001);
		assertEquals(14,comparison.getWeights().get("urllen"),0.0001);
	}
}
