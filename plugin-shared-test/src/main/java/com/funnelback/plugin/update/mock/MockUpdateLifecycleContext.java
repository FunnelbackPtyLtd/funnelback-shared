package com.funnelback.plugin.update.mock;

import com.funnelback.mock.helpers.MapBackedConfig;
import com.funnelback.mock.helpers.MapBackedPluginConfigurationFiles;
import com.funnelback.mock.helpers.PluginConfigurationFileSettingMock;
import com.funnelback.plugin.update.UpdateLifecycleContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;

import java.io.File;

public class MockUpdateLifecycleContext implements UpdateLifecycleContext, PluginConfigurationFileSettingMock {
    @Getter @Setter private String collectionName;
    @Getter @Setter private File searchHome;

    @Delegate private final MapBackedConfig mapBackedConfig = new MapBackedConfig();
    @Delegate private final MapBackedPluginConfigurationFiles mapBackedPluginConfigurationFiles = new MapBackedPluginConfigurationFiles();
}