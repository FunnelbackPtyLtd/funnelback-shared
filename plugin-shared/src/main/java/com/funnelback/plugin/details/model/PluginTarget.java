package com.funnelback.plugin.details.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PluginTarget {
    DATA_SOURCE("Data source"),
    RESULTS_PAGE("Results page");

    private final String target;

    @JsonValue
    public String getTarget() {
        return target;
    }
}
