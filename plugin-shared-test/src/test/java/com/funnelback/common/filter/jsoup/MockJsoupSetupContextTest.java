package com.funnelback.common.filter.jsoup;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class MockJsoupSetupContextTest {

    @Test
    public void testByDefaultConfigSettingsAreEmpty() {
        Assert.assertEquals(0, new MockJsoupSetupContext().getConfigKeysWithPrefix("").size());
    }
    
    @Test
    public void testConfigOptionsCanBeSetAndRead() {
        MockJsoupSetupContext setupContext = new MockJsoupSetupContext();
        setupContext.setConfigSetting("a", "b");
        Assert.assertEquals("b", setupContext.getConfigSetting("a"));
    }
    
    @Test
    public void testConfigOptionsCanBeReadByPrefix() {
        MockJsoupSetupContext setupContext = new MockJsoupSetupContext();
        setupContext.setConfigSetting("a", "aron");
        setupContext.setConfigSetting("aa", "ron");
        setupContext.setConfigSetting("aar", "on");
        setupContext.setConfigSetting("ro", "n");
        Set<String> keysWithPrefix = setupContext.getConfigKeysWithPrefix("aa");
        
        Assert.assertEquals(Set.of("aa", "aar"),  keysWithPrefix);
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
        
        Assert.assertTrue(matchingPattern.containsKey("a.b.c"));
        Assert.assertFalse(matchingPattern.containsKey("a.c.d"));
        Assert.assertTrue(matchingPattern.containsKey("a.d.c"));
        
        Assert.assertEquals(List.of("b"), matchingPattern.get("a.b.c"));
        Assert.assertEquals(List.of("d"), matchingPattern.get("a.d.c"));
    }
    
    @Test
    public void setAndReadPluginConfigurationFileAsString() {
        MockJsoupSetupContext ctx = new MockJsoupSetupContext();
        ctx.setPlugingConfigurationFileContent("foo.cfg", "hello");
        
        Assert.assertEquals("hello", ctx.pluginConfigurationFile("foo.cfg").get());
    }
    
    @Test
    public void setAndReadPluginConfigurationFileAsBytes() {
        MockJsoupSetupContext ctx = new MockJsoupSetupContext();
        ctx.setPlugingConfigurationFileContentAsBytes("foo.cfg", "hello".getBytes());
        
        Assert.assertEquals("hello", new String(ctx.pluginConfigurationFileAsBytes("foo.cfg").get()));
        Assert.assertEquals("hello", ctx.pluginConfigurationFile("foo.cfg").get());
    }
    
    @Test
    public void readMissingPluginConfigurationFile() {
        MockJsoupSetupContext ctx = new MockJsoupSetupContext();
        Assert.assertFalse(ctx.pluginConfigurationFile("foo.cfg").isPresent());
        Assert.assertFalse(ctx.pluginConfigurationFileAsBytes("foo.cfg").isPresent());
    }
}
