package com.funnelback.filter.api.mock;

import org.junit.Assert;
import org.junit.Test;

public class MockFilterContextTest {

    @Test
    public void testConfigOptions() {
        MockFilterContext filterContext = new MockFilterContext();
        Assert.assertEquals(0, filterContext.getConfigKeys().size());
        filterContext.setConfigValue("a", "foo");
        filterContext.setConfigValue("b", "bar");
        
        Assert.assertEquals(2, filterContext.getConfigKeys().size());
        Assert.assertEquals("foo", filterContext.getConfigValue("a").get());
        Assert.assertEquals("bar", filterContext.getConfigValue("b").get());
    }
    
    @Test
    public void setAndReadPluginConfigurationFileAsString() {
        MockFilterContext ctx = new MockFilterContext();
        ctx.setPlugingConfigurationFileContent("foo.cfg", "hello");
        
        Assert.assertEquals("hello", ctx.pluginConfigurationFile("foo.cfg").get());
    }
    
    @Test
    public void setAndReadPluginConfigurationFileAsBytes() {
        MockFilterContext ctx = new MockFilterContext();
        ctx.setPlugingConfigurationFileContentAsBytes("foo.cfg", "hello".getBytes());
        
        Assert.assertEquals("hello", new String(ctx.pluginConfigurationFileAsBytes("foo.cfg").get()));
        Assert.assertEquals("hello", ctx.pluginConfigurationFile("foo.cfg").get());
    }
    
    @Test
    public void readMissingPluginConfigurationFile() {
        MockFilterContext ctx = new MockFilterContext();
        Assert.assertFalse(ctx.pluginConfigurationFile("foo.cfg").isPresent());
        Assert.assertFalse(ctx.pluginConfigurationFileAsBytes("foo.cfg").isPresent());
    }
}
