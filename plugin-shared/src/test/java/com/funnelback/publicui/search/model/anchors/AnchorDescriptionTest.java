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
    
    @Test 
    public void testSecurityPathTraversalBypassPrevention() {
        // Security test to ensure that modification-after-validation vulnerability is fixed
        // This test ensures that any string modifications happen BEFORE pattern validation
        
        // Test potential bypass attempt with nested patterns like '....//' -> '../'
        String maliciousInput = "[k....//..]test"; 
        AnchorDescription a = new AnchorDescription(maliciousInput);
        
        // The resulting anchor text should not contain any potential bypass patterns
        // that could be created by string modification after pattern matching
        Assertions.assertFalse(a.getAnchorText().contains("../"), 
            "Anchor text should not contain path traversal patterns after processing");
        Assertions.assertFalse(a.getLinkType().contains("../"), 
            "Link type should not contain path traversal patterns after processing");
        
        // Additional test cases for comprehensive security coverage
        String[] maliciousInputs = {
            "[k....//]bypass",
            "test..//test",
            "[K]../../../etc/passwd",
            "normal[k..].."
        };
        
        for (String input : maliciousInputs) {
            AnchorDescription desc = new AnchorDescription(input);
            Assertions.assertFalse(desc.getAnchorText().contains("../"), 
                "Input '" + input + "' should not result in path traversal patterns");
            Assertions.assertFalse(desc.getLinkType().contains("../"), 
                "Input '" + input + "' link type should not contain path traversal patterns");
        }
    }
}