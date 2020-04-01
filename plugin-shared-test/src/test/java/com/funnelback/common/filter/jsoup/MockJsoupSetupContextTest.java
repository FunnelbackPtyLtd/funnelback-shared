package com.funnelback.common.filter.jsoup;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class MockJsoupSetupContextTest {

    @Test
    public void testByDefaultConfigSettingsAreEmpty() {
        Assert.assertEquals(0, new MockJsoupSetupContext().getConfigSettings().size());
    }
    
    @Test
    public void testConfigOptionsCanBeSetAndRead() {
        MockJsoupSetupContext setupContext = new MockJsoupSetupContext();
        setupContext.getConfigSettings().put("a", "b");
        Assert.assertEquals("b", setupContext.getConfigSetting("a"));
    }
    
    @Test
    public void testConfigOptionsCanBeReadByPrefix() {
        MockJsoupSetupContext setupContext = new MockJsoupSetupContext();
        setupContext.getConfigSettings().put("a", "aron");
        setupContext.getConfigSettings().put("aa", "ron");
        setupContext.getConfigSettings().put("aar", "on");
        setupContext.getConfigSettings().put("ro", "n");
        Set<String> keysWithPrefix = setupContext.getConfigKeysWithPrefix("aa");
        
        Assert.assertEquals(Set.of("aa", "aar"),  keysWithPrefix);
    }
    
    /**
     * Further testing is done in common see WildCardKeyMatcher
     */
    @Test
    public void testConfigOptionsCanBeReadByPattern() {
        MockJsoupSetupContext setupContext = new MockJsoupSetupContext();
        setupContext.getConfigSettings().put("a.b.c", "a");
        setupContext.getConfigSettings().put("a.c.d", "a");
        setupContext.getConfigSettings().put("a.d.c", "a");
        
        Map<String, List<String>> matchingPattern = setupContext.getConfigKeysMatchingPattern("a.*.c");
        
        Assert.assertTrue(matchingPattern.containsKey("a.b.c"));
        Assert.assertFalse(matchingPattern.containsKey("a.c.d"));
        Assert.assertTrue(matchingPattern.containsKey("a.d.c"));
        
        Assert.assertEquals(List.of("b"), matchingPattern.get("a.b.c"));
        Assert.assertEquals(List.of("d"), matchingPattern.get("a.d.c"));
    }
}
