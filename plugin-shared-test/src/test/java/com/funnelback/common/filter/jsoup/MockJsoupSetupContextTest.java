package com.funnelback.common.filter.jsoup;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MockJsoupSetupContextTest {

    @Test
    public void testByDefaultConfigSettingsAreEmpty() {
        Assertions.assertEquals(0, new MockJsoupSetupContext().getConfigKeysWithPrefix("").size());
    }
    
    @Test
    public void testConfigOptionsCanBeSetAndRead() {
        MockJsoupSetupContext setupContext = new MockJsoupSetupContext();
        setupContext.setConfigSetting("a", "b");
        Assertions.assertEquals("b", setupContext.getConfigSetting("a"));
    }
    
    @Test
    public void testConfigOptionsCanBeReadByPrefix() {
        MockJsoupSetupContext setupContext = new MockJsoupSetupContext();
        setupContext.setConfigSetting("a", "aron");
        setupContext.setConfigSetting("aa", "ron");
        setupContext.setConfigSetting("aar", "on");
        setupContext.setConfigSetting("ro", "n");
        Set<String> keysWithPrefix = setupContext.getConfigKeysWithPrefix("aa");
        
        Assertions.assertEquals(Set.of("aa", "aar"), keysWithPrefix);
    }
    
    /**
     * Further testing is done in common see WildCardKeyMatcher
     */
    @Test
    public void testConfigOptionsCanBeReadByPattern() {
        MockJsoupSetupContext setupContext = new MockJsoupSetupContext();
        setupContext.setConfigSetting("a.b.c", "a");
        setupContext.setConfigSetting("a.c.d", "a");
        setupContext.setConfigSetting("a.d.c", "a");
        
        Map<String, List<String>> matchingPattern = setupContext.getConfigKeysMatchingPattern("a.*.c");
        
        Assertions.assertTrue(matchingPattern.containsKey("a.b.c"));
        Assertions.assertFalse(matchingPattern.containsKey("a.c.d"));
        Assertions.assertTrue(matchingPattern.containsKey("a.d.c"));
        
        Assertions.assertEquals(List.of("b"), matchingPattern.get("a.b.c"));
        Assertions.assertEquals(List.of("d"), matchingPattern.get("a.d.c"));
    }
    
    @Test
    public void setAndReadPluginConfigurationFileAsString() {
        MockJsoupSetupContext ctx = new MockJsoupSetupContext();
        ctx.setPlugingConfigurationFileContent("foo.cfg", "hello");
        
        Assertions.assertEquals("hello", ctx.pluginConfigurationFile("foo.cfg").orElseThrow());
    }
    
    @Test
    public void setAndReadPluginConfigurationFileAsBytes() {
        MockJsoupSetupContext ctx = new MockJsoupSetupContext();
        ctx.setPlugingConfigurationFileContentAsBytes("foo.cfg", "hello".getBytes());
        
        Assertions.assertEquals("hello", new String(ctx.pluginConfigurationFileAsBytes("foo.cfg").orElseThrow()));
        Assertions.assertEquals("hello", ctx.pluginConfigurationFile("foo.cfg").orElseThrow());
    }
    
    @Test
    public void readMissingPluginConfigurationFile() {
        MockJsoupSetupContext ctx = new MockJsoupSetupContext();
        Assertions.assertFalse(ctx.pluginConfigurationFile("foo.cfg").isPresent());
        Assertions.assertFalse(ctx.pluginConfigurationFileAsBytes("foo.cfg").isPresent());
    }
}