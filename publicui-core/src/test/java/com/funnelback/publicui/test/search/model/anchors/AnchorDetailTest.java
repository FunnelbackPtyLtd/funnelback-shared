package com.funnelback.publicui.test.search.model.anchors;

import junit.framework.Assert;

import org.junit.Test;

import com.funnelback.publicui.search.model.anchors.AnchorDetail;


public class AnchorDetailTest {

	@Test
	public void testAnchorSetting() {
		AnchorDetail ad = new AnchorDetail("anchortext");
		Assert.assertEquals("anchortext",ad.getLinkAnchortext());
		Assert.assertEquals("anchortext",ad.getAnchortext());
		
		ad = new AnchorDetail("[k1]anchor text");
		Assert.assertEquals("[k1]anchor text",ad.getLinkAnchortext());
		Assert.assertEquals("anchor text",ad.getAnchortext());

		ad = new AnchorDetail("[K]anchor text");
		Assert.assertEquals("[K]anchor text",ad.getLinkAnchortext());
		Assert.assertEquals("anchor text",ad.getAnchortext());
	}
}
