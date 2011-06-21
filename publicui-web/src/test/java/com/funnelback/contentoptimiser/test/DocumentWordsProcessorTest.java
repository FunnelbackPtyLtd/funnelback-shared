package com.funnelback.contentoptimiser.test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;

import com.funnelback.contentoptimiser.DefaultDocumentWordsProcessor;
import com.funnelback.contentoptimiser.DocumentContentScoreBreakdown;
import com.funnelback.contentoptimiser.DocumentWordsProcessor;


public class DocumentWordsProcessorTest {

	@Test
	public void testObtainContent() {
		DocumentWordsProcessor dwp = new DefaultDocumentWordsProcessor("one two two two three four five five five_t five_h six");
		
		DocumentContentScoreBreakdown content = dwp.explainQueryTerm("five");
		Assert.assertEquals(content.getCount(), 2);
		Assert.assertEquals(1, content.getCount("t").intValue());
		Assert.assertEquals(1, content.getCount("h").intValue());
		Assert.assertNull(content.getCount("x"));
		
		Set<Map.Entry<String,Integer>> s = content.getCounts();
		Iterator<Map.Entry<String,Integer>> it = s.iterator();
		Assert.assertTrue(it.hasNext());
		Map.Entry<String, Integer> e = it.next();
		Assert.assertEquals("t", e.getKey());
		Assert.assertEquals(1, e.getValue().intValue());
		e = it.next();
		Assert.assertEquals("h", e.getKey());
		Assert.assertEquals(1, e.getValue().intValue());
		Assert.assertFalse(it.hasNext());
		
		Assert.assertEquals(80,content.getPercentageLess());
	}
	
	@Test
	public void testDocumentOverview() {
		DocumentWordsProcessor dwp = new DefaultDocumentWordsProcessor("one two two two three four five five six");
		String[] expectedFive =new String[] {"two","five","three","six","one"};
		Assert.assertTrue("Top five words were " + Arrays.toString(dwp.getTopFiveWords()) + " but expected " + Arrays.toString(expectedFive), Arrays.equals(expectedFive, dwp.getTopFiveWords()));
		Assert.assertEquals(9,dwp.totalWords());
		Assert.assertEquals(6, dwp.uniqueWords());
	}
	
}
