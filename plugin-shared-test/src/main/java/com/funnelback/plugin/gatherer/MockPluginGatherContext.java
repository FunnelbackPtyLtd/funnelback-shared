package com.funnelback.plugin.gatherer;

import java.io.File;

import com.funnelback.mock.helpers.MapBackedConfig;

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
