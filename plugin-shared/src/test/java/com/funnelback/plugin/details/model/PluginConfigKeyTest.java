package com.funnelback.plugin.details.model;

import org.junit.Assert;
import org.junit.Test;

public class PluginConfigKeyTest {

    @Test
    public void testKey() {
        var underTest = getConfigKey("test");
        Assert.assertEquals("plugin.pluginId.config.", underTest.getKeyPrefix());
        Assert.assertEquals("plugin.pluginId.config.test", underTest.getKey());
    }

    @Test
    public void testKeyWithWildcard() {
        var underTest = getConfigKey("foo.*");
        Assert.assertEquals("plugin.pluginId.config.foo.*", underTest.getKey());
        Assert.assertEquals("plugin.pluginId.config.foo.baz", underTest.getKey("baz"));
    }

    private PluginConfigKey getConfigKey(String id) {
        return new PluginConfigKey<>("pluginId", id,  "label", "desc", true,
            new PluginConfigKeyType(PluginConfigKeyType.Format.INTEGER), 100, null, null);
    }
}