package com.funnelback.plugin.search.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * To build the suggestion query which will query PADRE to a suggestion
 */
@RequiredArgsConstructor
public class SuggestionQuery {

    /** Initial query term for requesting suggestion */
    @Getter private final String partialQuery;

    /** How many suggestions to get */
    @Setter @Getter private Integer count = null;

    /** Sort options for returned suggestions */
    @Setter @Getter private Sort sort = Sort.DescendingWeight;

    /** alpha value */
    @Setter @Getter private double alpha;

    /** Category to scope suggestions */
    @Setter @Getter private String category;
}

