package com.funnelback.publicui.test.search.model.anchors;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.anchors.AnchorDescription;


public class AnchorDescriptionTest {

	@Test
	public void testEmpty() {
		AnchorDescription a = new AnchorDescription("blank anchortext");
		Assert.assertEquals(0,a.getLinksTo().size());
		Assert.assertEquals(0,a.getExternalLinkCount());
		Assert.assertEquals(0,a.getInternalLinkCount());
		Assert.assertEquals("blank anchortext",a.getAnchorText());
	}
	
	@Test
	public void testSpaceFolding() {
		AnchorDescription a = new AnchorDescription("blank anchortext");
		AnchorDescription b = new AnchorDescription("blank  anchortext ");
		Assert.assertEquals(a.getAnchorText(), b.getAnchorText());
	}
	
	@Test
	public void testLinkType() {
		AnchorDescription a = new AnchorDescription("[k1]blank anchortext");
		Assert.assertEquals("1",a.getLinkType());
		Assert.assertEquals("blank anchortext",a.getAnchorText());
	}
	
	@Test
	public void testClickDataLinkType() {
		AnchorDescription a = new AnchorDescription("[K]blank anchortext");
		Assert.assertEquals(" ",a.getLinkType());
		Assert.assertEquals(" K blank anchortext",a.getAnchorText());
	}
	
	@Test
	public void testExternal() {
		AnchorDescription a = new AnchorDescription("blank anchortext");
		Assert.assertEquals(0,a.getLinksTo().size());
		a.linkTo("-00000001");
		Assert.assertEquals(0,a.getLinksTo().size());
		Assert.assertEquals(1,a.getExternalLinkCount());
		Assert.assertEquals(0,a.getInternalLinkCount());
	}
	
	@Test
	public void testInternal() {
		AnchorDescription a = new AnchorDescription("blank anchortext");
		Assert.assertEquals(0,a.getLinksTo().size());
		Assert.assertFalse(a.getLinksTo().contains("00000101"));
		a.linkTo("00000101");
		Assert.assertEquals(1,a.getLinksTo().size());
		Assert.assertTrue(a.getLinksTo().contains("00000101"));
		Assert.assertEquals(0,a.getExternalLinkCount());
		Assert.assertEquals(1,a.getInternalLinkCount());
	}

}
