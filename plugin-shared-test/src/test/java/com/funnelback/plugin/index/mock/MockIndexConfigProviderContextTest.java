package com.funnelback.plugin.index.mock;

import org.junit.Assert;
import org.junit.Test;

public class MockIndexConfigProviderContextTest {

    @Test
    public void setAndReadPluginConfigurationFileAsString() {
        MockIndexConfigProviderContext ctx = new MockIndexConfigProviderContext();
        ctx.setPlugingConfigurationFileContent("foo.cfg", "hello");
        
        Assert.assertEquals("hello", ctx.pluginConfigurationFile("foo.cfg").get());
    }
    
    @Test
    public void setAndReadPluginConfigurationFileAsBytes() {
        MockIndexConfigProviderContext ctx = new MockIndexConfigProviderContext();
        ctx.setPlugingConfigurationFileContentAsBytes("foo.cfg", "hello".getBytes());
        
        Assert.assertEquals("hello", new String(ctx.pluginConfigurationFileAsBytes("foo.cfg").get()));
        Assert.assertEquals("hello", ctx.pluginConfigurationFile("foo.cfg").get());
    }
    
    @Test
    public void readMissingPluginConfigurationFile() {
        MockIndexConfigProviderContext ctx = new MockIndexConfigProviderContext();
        Assert.assertFalse(ctx.pluginConfigurationFile("foo.cfg").isPresent());
        Assert.assertFalse(ctx.pluginConfigurationFileAsBytes("foo.cfg").isPresent());
    }
}
