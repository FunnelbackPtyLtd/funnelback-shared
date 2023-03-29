package com.funnelback.plugin.details.model;

import lombok.NonNull;

public class PluginConfigKeyEncrypted<T> extends PluginConfigKey<T> {
    public PluginConfigKeyEncrypted(@NonNull String pluginId, @NonNull String id, @NonNull String label,
        @NonNull String description, @NonNull boolean required) {
        super(pluginId, id, label, description, required, new PluginConfigKeyType(PluginConfigKeyType.Format.PASSWORD), null, null, null);
    }

    public String getKeyPrefix() {
        return "plugin." + getPluginId() + ".encrypted.";
    }
}
