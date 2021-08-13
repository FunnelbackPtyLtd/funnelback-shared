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
    
    /**
     * Adds an external metadata mapping line in the external-metadata.cfg format.
     * 
     * The format is defined in:
     * https://docs.squiz.net/funnelback/docs/latest/build/data-sources/indexer-configuration/metadata/external-metadata.html
     * 
     * 
     * @param externalMetadataLine a single external metadata line.
     * @throws IllegalArgumentException if the externalMetadataLine is invalid.
     */
    public void addExternalMetadataLine(String externalMetadataLine)
        throws IllegalArgumentException;
}
