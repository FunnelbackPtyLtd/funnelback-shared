package com.funnelback.plugin.details.model;

import lombok.Getter;
import lombok.NonNull;

import java.util.List;
import java.util.regex.Pattern;

@Getter
public class PluginConfigKeyAllowedValue {
    public enum AllowedType {
        FIXED_LIST, REGEX_LIST;
    }

    @NonNull private final AllowedType type;
    @NonNull private final List<String> values;
    @NonNull private final Pattern regex;

    public PluginConfigKeyAllowedValue(@NonNull List <String> values) {
        this.regex = null;
        this.values = values;
        this.type = AllowedType.FIXED_LIST;
    }

    public PluginConfigKeyAllowedValue(@NonNull Pattern regex) {
        this.regex = regex;
        this.values = null;
        this.type = AllowedType.REGEX_LIST;
    }
}
