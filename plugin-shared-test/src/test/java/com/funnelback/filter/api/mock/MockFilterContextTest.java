package com.funnelback.filter.api.mock;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MockFilterContextTest {

    @Test
    public void testConfigOptions() {
        MockFilterContext filterContext = new MockFilterContext();
        Assertions.assertEquals(0, filterContext.getConfigKeys().size());
        filterContext.setConfigValue("a", "foo");
        filterContext.setConfigValue("b", "bar");
        
        Assertions.assertEquals(2, filterContext.getConfigKeys().size());
        Assertions.assertEquals("foo", filterContext.getConfigValue("a").get());
        Assertions.assertEquals("bar", filterContext.getConfigValue("b").get());
    }
    
    @Test
    public void setAndReadPluginConfigurationFileAsString() {
        MockFilterContext ctx = new MockFilterContext();
        ctx.setPlugingConfigurationFileContent("foo.cfg", "hello");
        
        Assertions.assertEquals("hello", ctx.pluginConfigurationFile("foo.cfg").get());
    }
    
    @Test
    public void setAndReadPluginConfigurationFileAsBytes() {
        MockFilterContext ctx = new MockFilterContext();
        ctx.setPlugingConfigurationFileContentAsBytes("foo.cfg", "hello".getBytes());
        
        Assertions.assertEquals("hello", new String(ctx.pluginConfigurationFileAsBytes("foo.cfg").get()));
        Assertions.assertEquals("hello", ctx.pluginConfigurationFile("foo.cfg").get());
    }

    @Test
    public void readMissingPluginConfigurationFile() {
        MockFilterContext ctx = new MockFilterContext();
        Assertions.assertFalse(ctx.pluginConfigurationFile("foo.cfg").isPresent());
        Assertions.assertFalse(ctx.pluginConfigurationFileAsBytes("foo.cfg").isPresent());
    }

    @Test
    public void setAndReadFilterConfigurationFileAsString() {
        MockFilterContext ctx = new MockFilterContext();
        ctx.setFilterConfigurationFileContent("foo.cfg", "hello");

        Assertions.assertEquals("hello", ctx.filterConfigurationFile("foo.cfg").get());
    }

    @Test
    public void setAndReadFilterConfigurationFileAsBytes() {
        MockFilterContext ctx = new MockFilterContext();
        ctx.setFilterConfigurationFileContentAsBytes("foo.cfg", "hello".getBytes());

        Assertions.assertEquals("hello", new String(ctx.filterConfigurationFileAsBytes("foo.cfg").get()));
        Assertions.assertEquals("hello", ctx.filterConfigurationFile("foo.cfg").get());
    }

    @Test
    public void readMissingFilterConfigurationFile() {
        MockFilterContext ctx = new MockFilterContext();
        Assertions.assertFalse(ctx.filterConfigurationFile("foo.cfg").isPresent());
        Assertions.assertFalse(ctx.filterConfigurationFileAsBytes("foo.cfg").isPresent());
    }
}