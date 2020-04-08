package com.funnelback.plugin.index.model.metadatamapping;

public enum MetadataSourceType {
    /**
     * Used when the metadata must come from XML documents.
     */
    XML,
    /**
     * Used when the metadata can come from either HTTP headers
     * or from HTML metadata.
     */
    HTML_OR_HTTP_HEADERS;
}
