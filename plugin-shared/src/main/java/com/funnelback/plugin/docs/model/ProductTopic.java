package com.funnelback.plugin.docs.model;

import lombok.RequiredArgsConstructor;

/**
 * Source documentation meta tag "product-topic"
 */
@RequiredArgsConstructor
public enum ProductTopic {
    DATA_SOURCES("Search data sources"),
    RESULTS_PAGE("Search results pages"),
    SEARCH_PACKAGES("Search packages"),

    ANALYTICS_REPORTING("Analytics and reporting"),
    APPLICATION_ADMINISTRATION("Application administration"),
    INDEXING("Search indexing"),
    INTEGRATION_DEVELOPMENT("Integration and development"),
    RANKING_SORTING("Search ranking and sorting"),
    SYSTEM_ADMINISTRATION("System administration");

    private final String topic;
}
