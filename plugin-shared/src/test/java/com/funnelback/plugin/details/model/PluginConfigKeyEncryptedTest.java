package com.funnelback.plugin.details.model;

import org.junit.Assert;
import org.junit.Test;

public class PluginConfigKeyEncryptedTest {
    @Test
    public void testKey() {
        var underTest = getConfigKey("test");
        Assert.assertEquals("plugin.pluginId.encrypted.", underTest.getKeyPrefix());
        Assert.assertEquals("plugin.pluginId.encrypted.test", underTest.getKey());
    }

    @Test
    public void testKeyWithWildcard() {
        var underTest = getConfigKey("foo.*");
        Assert.assertEquals("plugin.pluginId.encrypted.foo.*", underTest.getKey());
        Assert.assertEquals("plugin.pluginId.encrypted.foo.baz", underTest.getKey("baz"));
    }

    private PluginConfigKeyEncrypted getConfigKey(String id) {
        return new PluginConfigKeyEncrypted("pluginId", id,  "label", "desc", true);
    }
}