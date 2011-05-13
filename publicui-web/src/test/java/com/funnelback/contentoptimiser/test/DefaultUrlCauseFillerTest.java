package com.funnelback.contentoptimiser.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import com.funnelback.contentoptimiser.DefaultUrlCauseFiller;
import com.funnelback.contentoptimiser.RankingScore;
import com.funnelback.contentoptimiser.UrlCausesFiller;
import com.funnelback.contentoptimiser.UrlComparison;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.xml.impl.StaxStreamParser;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.xml.XmlParsingException;

public class DefaultUrlCauseFillerTest {

	@Test
	public void testFillHints () {
		UrlCausesFiller f = new DefaultUrlCauseFiller();
		UrlComparison comparison = new UrlComparison();

		f.fillHints(comparison);
		
		Assert.assertEquals(4,comparison.getHints().size());
	}

	
	@Test
	public void testConsumeResultPacket() throws XmlParsingException, IOException {
		StaxStreamParser parser = new StaxStreamParser();
		ResultPacket rp = parser.parse(FileUtils.readFileToString(new File("src/test/resources/padre-xml/explain-mockup.xml"), "UTF-8"));

		UrlCausesFiller f = new DefaultUrlCauseFiller();
		UrlComparison comparison = new UrlComparison();

		f.consumeResultPacket(comparison, rp);
		f.setImportantUrl("", comparison);

		
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

		
	}
}
