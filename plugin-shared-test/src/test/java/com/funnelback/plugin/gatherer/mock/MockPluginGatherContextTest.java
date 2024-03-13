package com.funnelback.plugin.gatherer.mock;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MockPluginGatherContextTest {

    @Test
    public void setAndReadPluginConfigurationFileAsString() {
        MockPluginGatherContext ctx = new MockPluginGatherContext();
        ctx.setPlugingConfigurationFileContent("foo.cfg", "hello");
        
        Assertions.assertEquals("hello", ctx.pluginConfigurationFile("foo.cfg").get());
    }
    
    @Test
    public void setAndReadPluginConfigurationFileAsBytes() {
        MockPluginGatherContext ctx = new MockPluginGatherContext();
        ctx.setPlugingConfigurationFileContentAsBytes("foo.cfg", "hello".getBytes());
        
        Assertions.assertEquals("hello", new String(ctx.pluginConfigurationFileAsBytes("foo.cfg").get()));
        Assertions.assertEquals("hello", ctx.pluginConfigurationFile("foo.cfg").get());
    }
    
    @Test
    public void readMissingPluginConfigurationFile() {
        MockPluginGatherContext ctx = new MockPluginGatherContext();
        Assertions.assertFalse(ctx.pluginConfigurationFile("foo.cfg").isPresent());
        Assertions.assertFalse(ctx.pluginConfigurationFileAsBytes("foo.cfg").isPresent());
    }
}