package com.funnelback.plugin.index.model.metadatamapping;

public enum MetadataType {
    TEXT_NOT_INDEXED_AS_DOCUMENT_CONTENT,
    /**
     * Metadata of this type will also have its value recorded in document content.
     *   
     * If metadata "bob" was mapped into "foo" then that document would match
     * both queries:
     * <p><ul>
     * <li>foo:bob
     * <li>bob
     * </ul><p>
     */
    TEXT_INDEXED_AS_DOCUMENT_CONTENT, 
    GEOSPATIAL,
    NUMERICAL,
    SECURITY,
    /**
     * Currently can only be set to metadata class "d".
     */
    DATE;
}
