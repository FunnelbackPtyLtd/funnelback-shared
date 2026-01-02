package com.funnelback.plugin.update.mock;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

class MockUpdateLifecycleContextTest {
    private MockUpdateLifecycleContext underTest;

    @BeforeEach
    void setup() {
        underTest = new MockUpdateLifecycleContext();
    }

    @Test
    void setAndReadCollectionName() {
        underTest.setCollectionName("bar");
        Assertions.assertEquals("bar", underTest.getCollectionName());
    }

    @Test
    void setAndReadCollectionSetting() {
        underTest.setConfigSetting("foo", "bar");
        Assertions.assertEquals("bar", underTest.getConfigSetting("foo"));
        Assertions.assertNull(underTest.getConfigSetting("baz"));
    }

    @Test
    void setAndReadSearchHome() {
        final File sh = new File("/tmp/test");
        underTest.setSearchHome(sh);
        Assertions.assertEquals(sh, underTest.getSearchHome());
    }

    @Test
    void setAndReadPluginConfigurationFileAsString() {
        underTest.setPlugingConfigurationFileContent("foo.cfg", "hello");

        Assertions.assertEquals("hello", underTest.pluginConfigurationFile("foo.cfg").get());
    }

    @Test
    void setAndReadPluginConfigurationFileAsBytes() {
        underTest.setPlugingConfigurationFileContentAsBytes("foo.cfg", "hello".getBytes());

        Assertions.assertEquals("hello", new String(underTest.pluginConfigurationFileAsBytes("foo.cfg").get()));
        Assertions.assertEquals("hello", underTest.pluginConfigurationFile("foo.cfg").get());
    }

    @Test
    void readMissingPluginConfigurationFile() {
        Assertions.assertFalse(underTest.pluginConfigurationFile("foo.cfg").isPresent());
        Assertions.assertFalse(underTest.pluginConfigurationFileAsBytes("foo.cfg").isPresent());
    }
}