package com.funnelback.plugin.gatherer.mock;

import java.io.File;

import com.funnelback.mock.helpers.ConfigSettingMock;
import com.funnelback.mock.helpers.MapBackedConfig;
import com.funnelback.plugin.gatherer.PluginGatherContext;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;

/**
 * 
 * A Mock PluginGatherContext that may be used when testing the PluginGatherer.
 * 
 * Example:
 * <pre>{@code 
 * MockPluginGatherContext pluginGatherContext = new MockPluginGatherContext();
 * // Set collection config setting 'foo=bar'.
 * pluginGatherContext.setConfigSetting("foo", "bar");
 * 
 * MockPluginStore pluginStore = new MockPluginStore();
 * 
 * PluginGatherer underTest = new MyPluginGatherer();
 * 
 * underTest.gather(pluginGatherContext, pluginStore);
 * }</pre>
 *
 */
public class MockPluginGatherContext implements PluginGatherContext, ConfigSettingMock {

    @Getter @Setter private File searchHome;

    @Getter @Setter private String collectionName;

    @Delegate private final MapBackedConfig mapBackedConfig = new MapBackedConfig();
}
