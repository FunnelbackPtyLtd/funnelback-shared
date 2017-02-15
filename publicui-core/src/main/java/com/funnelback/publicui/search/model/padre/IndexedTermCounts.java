package com.funnelback.publicui.search.model.padre;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * <p>Holds the count of each indexed term for a metadata class 'metadataClass'.</p>
 * 
 * @since 15.8
 */
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class IndexedTermCounts {
    
    /**
     * <p>The metadata class the terms are indexed under.</p>
     * 
     * @since 15.10
     */
    @Getter private final String metadataClass;
    
    /**
     * <p>Holds the count of occurrences of each term in the metadata class in the result set.</p>
     * 
     * <p>The Key is the indexed term (which may be truncated as per the indexer options used).
     * The Value is the number of times the term appears in the metadata class in the result set of
     * the query run.</p>
     * 
     * @since 15.10
     */
    @Getter private final Map<String, Long> termAndOccurrences = new HashMap<>();
 
}