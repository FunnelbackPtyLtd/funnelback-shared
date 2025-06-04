package com.funnelback.publicui.search.model.anchors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AnchorDescriptionTest {

    @Test
    public void testEmpty() {
        AnchorDescription a = new AnchorDescription("blank anchortext");
        Assertions.assertEquals(0, a.getLinksTo().size());
        Assertions.assertEquals(0, a.getExternalLinkCount());
        Assertions.assertEquals(0, a.getInternalLinkCount());
        Assertions.assertEquals("blank anchortext", a.getAnchorText());
    }
    
    @Test
    public void testSpaceFolding() {
        AnchorDescription a = new AnchorDescription("blank anchortext");
        AnchorDescription b = new AnchorDescription("blank  anchortext ");
        Assertions.assertEquals(a.getAnchorText(), b.getAnchorText());
    }
    
    @Test
    public void testLinkType() {
        AnchorDescription a = new AnchorDescription("[k1]blank anchortext");
        Assertions.assertEquals("1", a.getLinkType());
        Assertions.assertEquals("blank anchortext", a.getAnchorText());
    }
    
    @Test
    public void testClickDataLinkType() {
        AnchorDescription a = new AnchorDescription("[K]blank anchortext");
        Assertions.assertEquals("K", a.getLinkType());
        Assertions.assertEquals("blank anchortext", a.getAnchorText());
    }
    
    @Test
    public void testExternal() {
        AnchorDescription a = new AnchorDescription("blank anchortext");
        Assertions.assertEquals(0, a.getLinksTo().size());
        a.linkTo("-00000001");
        Assertions.assertEquals(0, a.getLinksTo().size());
        Assertions.assertEquals(1, a.getExternalLinkCount());
        Assertions.assertEquals(0, a.getInternalLinkCount());
    }
    
    @Test
    public void testInternal() {
        AnchorDescription a = new AnchorDescription("blank anchortext");
        Assertions.assertEquals(0, a.getLinksTo().size());
        Assertions.assertFalse(a.getLinksTo().contains("00000101"));
        a.linkTo("00000101");
        Assertions.assertEquals(1, a.getLinksTo().size());
        Assertions.assertTrue(a.getLinksTo().contains("00000101"));
        Assertions.assertEquals(0, a.getExternalLinkCount());
        Assertions.assertEquals(1, a.getInternalLinkCount());
    }
}