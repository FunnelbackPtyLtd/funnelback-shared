package com.funnelback.plugin.details.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PluginConfigKeyTest {

    @Test
    public void testKey() {
        var underTest = getConfigKey("test");
        Assertions.assertEquals("plugin.pluginId.config.", underTest.getKeyPrefix());
        Assertions.assertEquals("plugin.pluginId.config.test", underTest.getKey());
        Assertions.assertNull(underTest.getLongDescription());
    }

    @Test
    public void testKeyWithWildcard() {
        var underTest = getConfigKey("foo.*");
        Assertions.assertEquals("plugin.pluginId.config.foo.*", underTest.getKey());
        Assertions.assertEquals("plugin.pluginId.config.foo.baz", underTest.getKey("baz"));
    }

    @Test
    public void testKeyWithInvalidWildcard() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> getConfigKey("foo.*.*"));
    }

    @Test
    public void testKeyWithLongDesc() {
        var underTest = getConfigKeyWithLongDesc("foo");
        Assertions.assertEquals("plugin.pluginId.config.foo", underTest.getKey());
        Assertions.assertEquals("longDesc", underTest.getLongDescription());
    }

    private PluginConfigKey getConfigKey(String id) {
        return new PluginConfigKey<>("pluginId", id,  "label", "desc", true,
            new PluginConfigKeyType(PluginConfigKeyType.Format.INTEGER), 100, null, null);
    }

    private PluginConfigKey getConfigKeyWithLongDesc(String id) {
        return new PluginConfigKey<>("pluginId", id,  "label", "desc", "longDesc", true,
            new PluginConfigKeyType(PluginConfigKeyType.Format.INTEGER), 100, null, null);
    }
}