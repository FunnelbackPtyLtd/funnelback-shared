package com.funnelback.plugin.index.mock;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MockIndexConfigProviderContextTest {

    @Test
    public void setAndReadPluginConfigurationFileAsString() {
        MockIndexConfigProviderContext ctx = new MockIndexConfigProviderContext();
        ctx.setPlugingConfigurationFileContent("foo.cfg", "hello");
        
        Assertions.assertEquals("hello", ctx.pluginConfigurationFile("foo.cfg").get());
    }
    
    @Test
    public void setAndReadPluginConfigurationFileAsBytes() {
        MockIndexConfigProviderContext ctx = new MockIndexConfigProviderContext();
        ctx.setPlugingConfigurationFileContentAsBytes("foo.cfg", "hello".getBytes());
        
        Assertions.assertEquals("hello", new String(ctx.pluginConfigurationFileAsBytes("foo.cfg").get()));
        Assertions.assertEquals("hello", ctx.pluginConfigurationFile("foo.cfg").get());
    }
    
    @Test
    public void readMissingPluginConfigurationFile() {
        MockIndexConfigProviderContext ctx = new MockIndexConfigProviderContext();
        Assertions.assertFalse(ctx.pluginConfigurationFile("foo.cfg").isPresent());
        Assertions.assertFalse(ctx.pluginConfigurationFileAsBytes("foo.cfg").isPresent());
    }
}