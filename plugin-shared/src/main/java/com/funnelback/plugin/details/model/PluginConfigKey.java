package com.funnelback.plugin.details.model;

import com.funnelback.common.utils.SharedConfigKeyUtils;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public class PluginConfigKey<T> implements PluginDetailsBase {
    @Getter @NonNull private final String pluginId;
    @NonNull private final String id;
    @Getter @NonNull private final String label;
    @Getter @NonNull private final String description;
    @Getter @NonNull private final boolean required;
    @Getter @NonNull private final PluginConfigKeyType type;
    @Getter private final T defaultValue;
    @Getter private final PluginConfigKeyAllowedValue allowedValue;
    @Getter private final PluginConfigKeyConditional showIfKeyHasValue;

    public String getKey() {
        return getKeyPrefix() + id;
    }

    public String getKey(String ...wildcard) {
        return SharedConfigKeyUtils.getKeyWithWildcard(getKey(), Arrays.asList(wildcard));
    }

}
