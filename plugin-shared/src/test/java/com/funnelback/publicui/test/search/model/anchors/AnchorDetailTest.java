package com.funnelback.publicui.test.search.model.anchors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.funnelback.publicui.search.model.anchors.AnchorDetail;

public class AnchorDetailTest {

    @Test
    public void testAnchorSetting() {
        AnchorDetail ad = new AnchorDetail("anchortext");
        Assertions.assertEquals("anchortext", ad.getLinkAnchortext());
        Assertions.assertEquals("anchortext", ad.getAnchortext());
        
        ad = new AnchorDetail("[k1]anchor text");
        Assertions.assertEquals("[k1]anchor text", ad.getLinkAnchortext());
        Assertions.assertEquals("anchor text", ad.getAnchortext());

        ad = new AnchorDetail("[K]anchor text");
        Assertions.assertEquals("[K]anchor text", ad.getLinkAnchortext());
        Assertions.assertEquals("anchor text", ad.getAnchortext());
    }
}
