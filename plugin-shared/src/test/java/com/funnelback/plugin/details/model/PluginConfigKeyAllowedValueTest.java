package com.funnelback.plugin.details.model;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.regex.Pattern;

public class PluginConfigKeyAllowedValueTest {
    @Test
    public void testAllowedList() {
        final var list = List.of("a", "b");
        var underTest = new  PluginConfigKeyAllowedValue(list);
        Assert.assertEquals(list, underTest.getValues());
        Assert.assertEquals(PluginConfigKeyAllowedValue.AllowedType.FIXED_LIST, underTest.getType());
        Assert.assertNull(underTest.getRegex());
    }

    @Test
    public void testRegex() {
        final var regex = Pattern.compile("test");
        var underTest = new PluginConfigKeyAllowedValue(regex);
        Assert.assertNull(underTest.getValues());
        Assert.assertEquals(PluginConfigKeyAllowedValue.AllowedType.REGEX_LIST, underTest.getType());
        Assert.assertEquals(regex, underTest.getRegex());
    }
}