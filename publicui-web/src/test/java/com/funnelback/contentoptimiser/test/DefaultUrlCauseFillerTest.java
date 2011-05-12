package com.funnelback.contentoptimiser.test;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.contentoptimiser.DefaultUrlCauseFiller;
import com.funnelback.contentoptimiser.UrlCausesFiller;
import com.funnelback.contentoptimiser.UrlComparison;

public class DefaultUrlCauseFillerTest {

	@Test
	public void testFillCauses () {
		UrlCausesFiller f = new DefaultUrlCauseFiller();
		UrlComparison comparison = new UrlComparison();
		f.addUrl("one", comparison);
		f.setImportantUrl("one", comparison);

	//	f.FillCauses(comparison);
		
		Assert.assertTrue(comparison.getUrls().size() != 0);
		Assert.assertTrue(comparison.getUrls().get(0).getCauses().size() != 0);
		Assert.assertTrue(comparison.getImportantOne().getCauses().size() != 0);
		Assert.assertTrue(comparison.getHints().size() != 0);
	}
	
	@Test
	public void testSetImportantOne() {
		UrlCausesFiller f = new DefaultUrlCauseFiller();
		UrlComparison comparison = new UrlComparison();

		Assert.assertNull(comparison.getImportantOne());
		
		String url = "url";
		
		f.setImportantUrl(url, comparison);
		
		Assert.assertNotNull(comparison.getImportantOne());
		Assert.assertEquals(url, comparison.getImportantOne().getUrl());
	}
}
