package com.funnelback.publicui.search.model.facetednavigation;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class FacetSelectedDetails {
    @Getter private final String facetName;
    @Getter private final String extraParameter;
    @Getter private final String value;
}