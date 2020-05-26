package com.funnelback.plugin.index.consumers;

import com.google.common.collect.ListMultimap;

public interface ExternalMetadataConsumer {

    /**
     * Adds an external metadata mapping.
     * 
     * @param URL URL prefix. Must include a full hostname, if no protocol specified then http:// assumed
     * e.g. https://www.example.org/movies
     * @param metadata a multimap of one or more metadata keys and values.
     * @throws IllegalArgumentException when one or more of the arguments is not valid.
     */
    public void addMetadataToPrefix(String URL, ListMultimap<String, String> metadata)
        throws IllegalArgumentException;
}
