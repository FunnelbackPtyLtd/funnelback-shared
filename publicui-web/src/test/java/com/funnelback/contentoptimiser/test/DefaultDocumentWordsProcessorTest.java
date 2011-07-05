package com.funnelback.contentoptimiser.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.funnelback.contentoptimiser.DocumentContentScoreBreakdown;
import com.funnelback.contentoptimiser.processors.DocumentWordsProcessor;
import com.funnelback.contentoptimiser.processors.impl.DefaultDocumentWordsProcessor;
import com.funnelback.publicui.search.model.anchors.AnchorDescription;
import com.funnelback.publicui.search.model.anchors.AnchorModel;
import com.funnelback.publicui.search.model.collection.Collection;


public class DefaultDocumentWordsProcessorTest {

	private AnchorModel anchors;
	
	@Before
	public void setupAnchors() {
		anchors = new AnchorModel();
		anchors.setAnchors(new ArrayList<AnchorDescription>());
	}
	
	@Test
	public void testObtainContent() {
		AnchorDescription anchorDescription = new AnchorDescription("[k1]five text");
		anchorDescription.linkTo("0");
		anchorDescription.linkTo("1");
		anchorDescription.linkTo("2");
		
		AnchorDescription anchorDescription2 = new AnchorDescription("[k0]five text");
		anchorDescription2.linkTo("0");
		anchorDescription2.linkTo("1");
		
		AnchorDescription anchorDescription3 = new AnchorDescription("[K]anchor five");
		anchorDescription3.linkTo("-1");
		
		anchors.getAnchors().add(anchorDescription);
		anchors.getAnchors().add(anchorDescription2);
		anchors.getAnchors().add(anchorDescription3);
		
		DocumentWordsProcessor dwp = new DefaultDocumentWordsProcessor("one two two two three four five five five_t five_h six", anchors);
		
		DocumentContentScoreBreakdown content = dwp.explainQueryTerm("five",new Collection("test1", null));
		Assert.assertEquals(content.getCount(), 2);
		Assert.assertEquals(1, content.getCount("t").intValue());
		Assert.assertEquals(1, content.getCount("h").intValue());
		Assert.assertEquals(5, content.getCount("k").intValue());
		Assert.assertEquals(1, content.getCount("K").intValue());
		Assert.assertNull(content.getCount("x"));
		
		Set<Map.Entry<String,Integer>> s = content.getCounts();
		Iterator<Map.Entry<String,Integer>> it = s.iterator();
		Assert.assertTrue(it.hasNext());
		Map.Entry<String, Integer> e = it.next();
		Assert.assertEquals("t", e.getKey());
		Assert.assertEquals(1, e.getValue().intValue());
		e = it.next();
		Assert.assertEquals("k", e.getKey());
		Assert.assertEquals(5, e.getValue().intValue());
		e = it.next();
		Assert.assertEquals("h", e.getKey());
		Assert.assertEquals(1, e.getValue().intValue());
		e = it.next();
		Assert.assertEquals("K", e.getKey());
		Assert.assertEquals(1, e.getValue().intValue());
		Assert.assertFalse(it.hasNext());
		
		Assert.assertEquals(80,content.getPercentageLess());
	}
	
	@Test
	public void testDocumentOverview() {
		DocumentWordsProcessor dwp = new DefaultDocumentWordsProcessor("one two two two three four five five six",anchors);
		String[] expectedFive =new String[] {"two","five","three","six","one"};
		Assert.assertTrue("Top five words in document incorrect ", Arrays.equals(expectedFive, dwp.getTopFiveWords(new ArrayList<String>(),"_")));
		Assert.assertEquals(9,dwp.totalWords());
		Assert.assertEquals(6, dwp.uniqueWords());
	}
	
}

