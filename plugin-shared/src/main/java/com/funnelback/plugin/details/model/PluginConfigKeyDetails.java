package com.funnelback.plugin.details.model;

public interface PluginConfigKeyDetails<T> extends PluginDetailsBase {

    default T getDefaultValue() {
        return null;
    }

    default PluginConfigKeyAllowedValue getAllowedValue() {
        return null;
    }

    default PluginConfigKeyConditional getShowIfKeyHasValue() {
        return null;
    }
}
