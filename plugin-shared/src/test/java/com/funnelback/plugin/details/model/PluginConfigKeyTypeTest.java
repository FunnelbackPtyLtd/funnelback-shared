package com.funnelback.plugin.details.model;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PluginConfigKeyTypeTest {
    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testType() {
        var underTest = new PluginConfigKeyType(PluginConfigKeyType.Format.INTEGER);
        Assert.assertEquals(PluginConfigKeyType.Format.INTEGER, underTest.getType());
        Assert.assertNull(underTest.getSubtype());
    }

    @Test
    public void testSubtype() {
        var underTest = new PluginConfigKeyType(PluginConfigKeyType.Format.ARRAY, PluginConfigKeyType.Format.STRING);
        Assert.assertEquals(PluginConfigKeyType.Format.ARRAY, underTest.getType());
        Assert.assertEquals(PluginConfigKeyType.Format.STRING, underTest.getSubtype());
    }

    @Test
    public void  testMissingSubtype() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Type 'ARRAY' requires to provide subtype but found null");
        new PluginConfigKeyType(PluginConfigKeyType.Format.ARRAY);
    }
}
