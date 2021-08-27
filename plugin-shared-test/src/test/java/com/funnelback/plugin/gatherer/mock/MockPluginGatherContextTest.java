package com.funnelback.plugin.gatherer.mock;

import org.junit.Assert;
import org.junit.Test;

public class MockPluginGatherContextTest {

    @Test
    public void setAndReadPluginConfigurationFileAsString() {
        MockPluginGatherContext ctx = new MockPluginGatherContext();
        ctx.setPlugingConfigurationFileContent("foo.cfg", "hello");
        
        Assert.assertEquals("hello", ctx.pluginConfigurationFile("foo.cfg").get());
    }
    
    @Test
    public void setAndReadPluginConfigurationFileAsBytes() {
        MockPluginGatherContext ctx = new MockPluginGatherContext();
        ctx.setPlugingConfigurationFileContentAsBytes("foo.cfg", "hello".getBytes());
        
        Assert.assertEquals("hello", new String(ctx.pluginConfigurationFileAsBytes("foo.cfg").get()));
        Assert.assertEquals("hello", ctx.pluginConfigurationFile("foo.cfg").get());
    }
    
    @Test
    public void readMissingPluginConfigurationFile() {
        MockPluginGatherContext ctx = new MockPluginGatherContext();
        Assert.assertFalse(ctx.pluginConfigurationFile("foo.cfg").isPresent());
        Assert.assertFalse(ctx.pluginConfigurationFileAsBytes("foo.cfg").isPresent());
    }
}
