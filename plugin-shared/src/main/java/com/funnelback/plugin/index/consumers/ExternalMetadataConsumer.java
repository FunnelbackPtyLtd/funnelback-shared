package com.funnelback.plugin.index.consumers;

import com.google.common.collect.ListMultimap;

@FunctionalInterface
public interface ExternalMetadataConsumer {

    /**
     * Adds an external metadata mapping.
     * 
     * @param URL URL prefix. Must include a full hostname, if no protocol specified then http:// assumed
     * e.g. https://www.example.org/movies
     * @param metadata a multimap of one or more metadata keys and values.
     * 
     */
    public void addMetadataToPrefix(String URL, ListMultimap<String, String> metadata);
}
