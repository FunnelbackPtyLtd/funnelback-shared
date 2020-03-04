package com.funnelback.filter.api;

public interface DocumentTypeFactory {
    
    /**
     * Creates a DocumentType from HTTP header Content-Type.
     * 
     * @param contentTypeHeader the value of a HTTP Content-Type header which will be used to create 
     * DocumentType.
     * @return a DocumentType constructed from the given contentType.
     */
    public DocumentType fromContentTypeHeader(String contentTypeHeader);
}
