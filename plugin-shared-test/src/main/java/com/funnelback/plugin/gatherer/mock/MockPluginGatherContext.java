package com.funnelback.plugin.gatherer.mock;

import java.io.File;

import com.funnelback.mock.helpers.MapBackedConfig;
import com.funnelback.plugin.gatherer.PluginGatherContext;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;

/**
 * TODO
 *
 */
public class MockPluginGatherContext implements PluginGatherContext {

    @Getter @Setter private File searchHome;

    @Getter @Setter private String collectionName;

    @Delegate private final MapBackedConfig mapBackedConfig = new MapBackedConfig();
}
