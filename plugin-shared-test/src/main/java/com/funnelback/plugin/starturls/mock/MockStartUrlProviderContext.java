package com.funnelback.plugin.starturls.mock;

import com.funnelback.mock.helpers.MapBackedConfig;
import com.funnelback.mock.helpers.MapBackedPluginConfigurationFiles;
import com.funnelback.plugin.starturl.StartUrlProviderContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;

import java.io.File;

/**
 *
 * A Mock StartUrlProviderContext that may be used when testing the StartUrlProvider.
 *
 * Example:
 * <pre>{@code
 * StartUrlProviderContext context = new MockStartUrlProviderContext();
 * // Set collection config setting 'foo=bar'.
 * context.setConfigSetting("foo", "bar");
 *
 * MockPluginStore pluginStore = new MockPluginStore();
 *
 * StartUrlProvider underTest = new MyPluginStartUrlProvider();
 *
 * List<URL> urlList = underTest.extraUrls(context);
 * }</pre>
 *
 */
public class MockStartUrlProviderContext implements StartUrlProviderContext {

        @Getter @Setter private File searchHome;

        @Getter @Setter private String collectionName;

        @Getter @Setter private String configSetting;

        @Delegate private final MapBackedConfig mapBackedConfig = new MapBackedConfig();

        @Delegate private final MapBackedPluginConfigurationFiles mapBackedPluginConfigurationFiles = new MapBackedPluginConfigurationFiles();

}
