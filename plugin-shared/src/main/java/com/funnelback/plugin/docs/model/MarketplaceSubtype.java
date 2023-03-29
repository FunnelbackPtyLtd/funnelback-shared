package com.funnelback.plugin.docs.model;

import lombok.RequiredArgsConstructor;

/**
 * Source documentation meta tag "marketplace-subtype"
 */
@RequiredArgsConstructor
public enum MarketplaceSubtype {
    FACETED_NAVIGATION_SORT("Faceted navigation custom sort"),
    FILTER("Filter"),
    GATHERER("Custom gatherer"),
    INDEXING("Indexing"),
    SERVLET_FILTER("Search servlet filter"),
    SEARCH_LIFECYCLE("Search lifecycle");

    private final String type;
}
