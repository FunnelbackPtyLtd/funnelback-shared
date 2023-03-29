package com.funnelback.plugin.details.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class PluginConfigFile {
    @NonNull private final String name;
    @NonNull private final String label;
    @NonNull private final String description;
    @NonNull private final String format;
    @NonNull private final boolean required;
}
