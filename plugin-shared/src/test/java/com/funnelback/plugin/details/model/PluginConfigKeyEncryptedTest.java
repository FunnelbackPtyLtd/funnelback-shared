package com.funnelback.plugin.details.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PluginConfigKeyEncryptedTest {
    @Test
    public void testKey() {
        var underTest = getConfigKey("test");
        Assertions.assertEquals("plugin.pluginId.encrypted.", underTest.getKeyPrefix());
        Assertions.assertEquals("plugin.pluginId.encrypted.test", underTest.getKey());
        Assertions.assertNull(underTest.getLongDescription());
    }

    @Test
    public void testKeyWithWildcard() {
        var underTest = getConfigKey("foo.*");
        Assertions.assertEquals("plugin.pluginId.encrypted.foo.*", underTest.getKey());
        Assertions.assertEquals("plugin.pluginId.encrypted.foo.baz", underTest.getKey("baz"));
    }

    @Test
    public void testKeyWithInvalidWildcard() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> getConfigKey("foo.*.*"));
    }

    @Test
    public void testKeyWithLongDesc() {
        var underTest = getConfigKeyWithLongDesc("foo");
        Assertions.assertEquals("plugin.pluginId.encrypted.foo", underTest.getKey());
        Assertions.assertEquals("longDesc", underTest.getLongDescription());
    }

    private PluginConfigKeyEncrypted getConfigKey(String id) {
        return new PluginConfigKeyEncrypted("pluginId", id,  "label", "desc", true);
    }

    private PluginConfigKeyEncrypted getConfigKeyWithLongDesc(String id) {
        return new PluginConfigKeyEncrypted("pluginId", id,  "label", "desc", "longDesc", true);
    }

}