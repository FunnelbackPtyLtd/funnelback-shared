package com.funnelback.plugin.details.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.regex.Pattern;

public class PluginConfigKeyAllowedValueTest {
    @Test
    public void testAllowedList() {
        final var list = List.of("a", "b");
        var underTest = new  PluginConfigKeyAllowedValue<>(list);
        Assertions.assertEquals(list, underTest.getValues());
        Assertions.assertEquals(PluginConfigKeyAllowedValue.AllowedType.FIXED_LIST, underTest.getType());
        Assertions.assertNull(underTest.getRegex());
    }

    @Test
    public void testRegex() {
        final var regex = Pattern.compile("test");
        var underTest = new PluginConfigKeyAllowedValue<>(regex);
        Assertions.assertNull(underTest.getValues());
        Assertions.assertEquals(PluginConfigKeyAllowedValue.AllowedType.REGEX_LIST, underTest.getType());
        Assertions.assertEquals(regex, underTest.getRegex());
    }
}