package com.funnelback.plugin.details.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PluginTarget {
    DATA_SOURCE("Data source"),
    RESULTS_PAGE("Results page");

    private final String target;
}
