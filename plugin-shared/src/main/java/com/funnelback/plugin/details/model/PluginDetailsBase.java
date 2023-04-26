package com.funnelback.plugin.details.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface PluginDetailsBase {
    String getPluginId();

    @JsonIgnore
    default String getKeyPrefix() {
        return "plugin." + getPluginId() + ".config.";
    }
}
