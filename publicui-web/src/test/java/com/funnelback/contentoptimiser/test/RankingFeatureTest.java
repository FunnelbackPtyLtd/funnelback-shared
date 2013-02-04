package com.funnelback.contentoptimiser.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.RankingFeature;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.RankingFeatureMaxOther;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.RankingFeatureMaxPossible;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.RankingFeatureMaxPossibleMultiWordOnly;
import com.funnelback.publicui.xml.XmlParsingException;
import com.funnelback.publicui.xml.padre.StaxStreamParser;


public class RankingFeatureTest {

	@Test
	public void testHintMaxOther() throws XmlParsingException, IOException {
		StaxStreamParser parser = new StaxStreamParser();
		ResultPacket rp = parser.parse(FileUtils.readFileToString(new File("src/test/resources/padre-xml/explain-mockup.xml"), "UTF-8"));
		

		RankingFeature h = new RankingFeatureMaxOther("name","content",rp);
		
		h.rememberScore(0.8f,""+1);
		h.rememberScore(0.6f,""+2);
		h.rememberScore(0.5f,""+3);
		h.rememberScore(0.4f,""+4);
		
		h.caculateWin(0.6f, 0.9f);
		
		assertTrue(h.isInteresting());
		assertEquals(0.2f,h.getWin(),0.00001f);

		// All scores the same, but we could do better
		h = new RankingFeatureMaxOther("name","content",rp);
		h.rememberScore(0.7f,""+1);
		h.rememberScore(0.7f,""+2);
		h.rememberScore(0.7f,""+3);
		h.rememberScore(0.7f,""+4);
		h.caculateWin(0.7f, 0.8f);
		assertEquals(0.0f,h.getWin(),0.00001f);
		assertTrue(h.isInteresting());

		// All scores the same except our lower score
		h = new RankingFeatureMaxOther("name","content",rp);
		h.rememberScore(0.7f,""+1);
		h.rememberScore(0.7f,""+2);
		h.rememberScore(0.7f,""+3);
		h.rememberScore(0.7f,""+4);
		h.caculateWin(0.6f, 0.8f);
		assertTrue(h.isInteresting());
		assertEquals(0.1f,h.getWin(),0.00001f);

		// We're winning this feature, but we could be better
		h = new RankingFeatureMaxOther("name","content",rp);
		h.rememberScore(0.6f,""+1);
		h.rememberScore(0.6f,""+2);
		h.rememberScore(0.6f,""+3);
		h.rememberScore(0.6f,""+4);
		h.caculateWin(0.7f, 0.8f);
		assertEquals(-0.1f,h.getWin(),0.00001f);
		assertTrue(h.isInteresting());		

		h = new RankingFeatureMaxOther("name","content",rp);
		assertFalse(h.isInteresting());
	}

	@Test public void testHintMaxPossibleMultiwordOnly() throws XmlParsingException, IOException {
		StaxStreamParser parser = new StaxStreamParser();
		ResultPacket rp = parser.parse(FileUtils.readFileToString(new File("src/test/resources/padre-xml/explain-mockup.xml"), "UTF-8"));
		
		RankingFeature h = new RankingFeatureMaxPossibleMultiWordOnly("name","content",rp);
		h.rememberScore(0.8f,""+1);
		h.rememberScore(0.6f,""+2);
		h.rememberScore(0.5f,""+3);
		h.rememberScore(0.4f,""+4);
		h.caculateWin(0.6f, 0.9f);
		
		assertTrue(h.isInteresting());
		assertEquals(0.3f,h.getWin(),0.00001f);
		
		// All scores the same, but we could do better
		h = new RankingFeatureMaxPossibleMultiWordOnly("name","content",rp);
		h.rememberScore(0.7f,""+1);
		h.rememberScore(0.7f,""+2);
		h.rememberScore(0.7f,""+3);
		h.rememberScore(0.7f,""+4);
		h.caculateWin(0.7f, 0.8f);
		assertEquals(0.1f,h.getWin(),0.00001f);
		assertTrue(h.isInteresting());
		
		// We're winning this feature, but we could be better
		h = new RankingFeatureMaxPossibleMultiWordOnly("name","content",rp);
		h.rememberScore(0.6f,""+1);
		h.rememberScore(0.6f,""+2);
		h.rememberScore(0.6f,""+3);
		h.rememberScore(0.6f,""+4);
		h.caculateWin(0.7f, 0.8f);
		assertEquals(0.1f,h.getWin(),0.00001f);
		assertTrue(h.isInteresting());

		// all scores the same, and minimum score
		h = new RankingFeatureMaxPossibleMultiWordOnly("name","content",rp);
		h.rememberScore(0.0f,""+1);
		h.rememberScore(0.0f,""+2);
		h.rememberScore(0.0f,""+3);
		h.rememberScore(0.0f,""+4);
		h.caculateWin(0.0f, 0.8f);
		assertEquals(0.8f,h.getWin(),0.00001f);
		assertTrue(h.isInteresting());

		// all scores the same, and maximum score
		h = new RankingFeatureMaxPossibleMultiWordOnly("name","content",rp);
		h.rememberScore(0.1f,""+1);
		h.rememberScore(0.1f,""+2);
		h.rememberScore(0.1f,""+3);
		h.rememberScore(0.1f,""+4);		
		h.caculateWin(0.1f, 0.1f);
		assertEquals(0.0f,h.getWin(),0.00001f);
		assertTrue(h.isInteresting());

		h = new RankingFeatureMaxPossibleMultiWordOnly("name","content",rp);
		assertFalse(h.isInteresting());
		
		ResultPacket oneWordQuery = parser.parse(FileUtils.readFileToString(new File("src/test/resources/padre-xml/complex.xml"), "UTF-8"));
		h = new RankingFeatureMaxPossibleMultiWordOnly("name","content",oneWordQuery);
		h.rememberScore(0.6f,""+1);
		h.rememberScore(0.6f,""+2);
		h.rememberScore(0.6f,""+3);
		h.rememberScore(0.6f,""+4);
		h.caculateWin(0.7f, 0.8f);
		assertEquals(0.0f,h.getWin(),0.00001f);
		assertTrue(h.isInteresting());
	}
	
	@Test
	public void testHintMaxPossible() throws XmlParsingException, IOException {
		StaxStreamParser parser = new StaxStreamParser();
		ResultPacket rp = parser.parse(FileUtils.readFileToString(new File("src/test/resources/padre-xml/explain-mockup.xml"), "UTF-8"));
		
		
		RankingFeature h = new RankingFeatureMaxPossible("name","content",rp);
		h.rememberScore(0.8f,""+1);
		h.rememberScore(0.6f,""+2);
		h.rememberScore(0.5f,""+3);
		h.rememberScore(0.4f,""+4);
		h.caculateWin(0.6f, 0.9f);
		
		assertTrue(h.isInteresting());
		assertEquals(0.3f,h.getWin(),0.00001f);
		
		// All scores the same, but we could do better
		h = new RankingFeatureMaxPossible("name","content",rp);
		h.rememberScore(0.7f,""+1);
		h.rememberScore(0.7f,""+2);
		h.rememberScore(0.7f,""+3);
		h.rememberScore(0.7f,""+4);
		h.caculateWin(0.7f, 0.8f);
		assertEquals(0.1f,h.getWin(),0.00001f);
		assertTrue(h.isInteresting());
		
		// We're winning this feature, but we could be better
		h = new RankingFeatureMaxPossible("name","content",rp);
		h.rememberScore(0.6f,""+1);
		h.rememberScore(0.6f,""+2);
		h.rememberScore(0.6f,""+3);
		h.rememberScore(0.6f,""+4);
		h.caculateWin(0.7f, 0.8f);
		assertEquals(0.1f,h.getWin(),0.00001f);
		assertTrue(h.isInteresting());

		// all scores the same, and minimum score
		h = new RankingFeatureMaxPossible("name","content",rp);
		h.rememberScore(0.0f,""+1);
		h.rememberScore(0.0f,""+2);
		h.rememberScore(0.0f,""+3);
		h.rememberScore(0.0f,""+4);
		h.caculateWin(0.0f, 0.8f);
		assertEquals(0.8f,h.getWin(),0.00001f);
		assertTrue(h.isInteresting());

		// all scores the same, and maximum score
		h = new RankingFeatureMaxPossible("name","content",rp);
		h.rememberScore(0.1f,""+1);
		h.rememberScore(0.1f,""+2);
		h.rememberScore(0.1f,""+3);
		h.rememberScore(0.1f,""+4);		
		h.caculateWin(0.1f, 0.1f);
		assertEquals(0.0f,h.getWin(),0.00001f);
		assertTrue(h.isInteresting());

		h = new RankingFeatureMaxPossible("name","content",rp);
		assertFalse(h.isInteresting());
	}
}



