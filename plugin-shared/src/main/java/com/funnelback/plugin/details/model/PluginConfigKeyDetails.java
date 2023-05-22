package com.funnelback.plugin.details.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.funnelback.plugin.details.model.deserializer.PluginConfigKeyDetailsDeserializer;

@JsonDeserialize(builder = PluginConfigKeyDetailsDeserializer.class)
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
