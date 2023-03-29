package com.funnelback.plugin.details.model;

public interface PluginDetailsBase {
    String getPluginId();

    default String getKeyPrefix() {
        return "plugin." + getPluginId() + ".config.";
    }
}
