package com.funnelback.plugin.details.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface PluginDetailsBase {

    String PLUGIN_PREFIX = "plugin.";
    String PLUGIN_CONFIG_QUALIFIER = ".config.";
    String PLUGIN_ENCRYPTED_QUALIFIER = ".encrypted.";

    String getPluginId();

    @JsonIgnore default String getKeyPrefix() {
        return PLUGIN_PREFIX + getPluginId() + PLUGIN_CONFIG_QUALIFIER;
    }
}
