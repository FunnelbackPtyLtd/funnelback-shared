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
}
