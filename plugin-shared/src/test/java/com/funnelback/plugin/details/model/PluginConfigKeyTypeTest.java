package com.funnelback.plugin.details.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PluginConfigKeyTypeTest {

    @Test
    public void testType() {
        var underTest = new PluginConfigKeyType(PluginConfigKeyType.Format.INTEGER);
        Assertions.assertEquals(PluginConfigKeyType.Format.INTEGER, underTest.getType());
        Assertions.assertNull(underTest.getSubtype());
    }

    @Test
    public void testSubtype() {
        var underTest = new PluginConfigKeyType(PluginConfigKeyType.Format.ARRAY, PluginConfigKeyType.Format.STRING);
        Assertions.assertEquals(PluginConfigKeyType.Format.ARRAY, underTest.getType());
        Assertions.assertEquals(PluginConfigKeyType.Format.STRING, underTest.getSubtype());
    }

    @Test
    public void  testMissingSubtype() {
        var exception = Assertions.assertThrows(IllegalArgumentException.class, () -> new PluginConfigKeyType(PluginConfigKeyType.Format.ARRAY));
        Assertions.assertEquals("Type 'ARRAY' requires to provide subtype but found null", exception.getMessage());
    }
}
