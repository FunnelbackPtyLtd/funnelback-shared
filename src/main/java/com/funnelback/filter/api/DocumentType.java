package com.funnelback.filter.api;

import com.funnelback.filter.ContentTypeDocumentType;

/**
 * Holds the information describing the type of the document.
 * 
 * <p>The type of the document such as HTML, PDF, XML etc. This does
 * not include the charset.</p>
 *
 */
public interface DocumentType {
    
    /**
     * Simple html MIME type which should be used when a filter converts a document to html.
     */
    public static final DocumentType MIME_HTML_TEXT = fromContentType("text/html");
    
    /**
     * Simple json MIME type which should be used when a filter converts a document to json.
     */
    public static final DocumentType MIME_JSON_TEXT = fromContentType("text/json");

    /**
     * Simple xml MIME type which should be used when a filter converts a document to xml.
     */
    public static final DocumentType MIME_XML_TEXT = fromContentType("text/xml");

    /**
     * Simple xhtml MIME type which should be used when a filter converts a document to xhtml.
     */
    public static final DocumentType MIME_XHTML_TEXT = fromContentType("text/xhtml");

    /**
     * A mime type that is unknown. Filters may be written to detect this and replace the 
     * mime type with something better. 
     */
    public static final DocumentType MIME_UNKNOWN = fromContentType("application/octet-stream");
    
    /**
     * A document type for plain text documents.
     */
    public static final DocumentType MIME_TEXT_PLAIN = fromContentType("text/plain");

    /**
     * 
     * @return true if the document type is considered to be HTML, includes
     * xhtml, otherwise false.
     */
    public boolean isHTML();
    
    /**
     * 
     * @return true if the document type is considered to be XML, includes
     * xhtml, otherwise false.
     */
    public boolean isXML();
    
    /**
     * 
     * @return true if the document type is considered to be JSON,
     * otherwise false.
     */
    public boolean isJSON();
        
    /**
     * Gets the document type as a Content-Type header value.
     * 
     * @return the document type as a HTTP compatible Content-Type.
     */
    public String asContentType();
    
    /**
     * Creates a DocumentType from HTTP header Content-Type.
     * 
     * @param contentType the HTTP content type which will be used to create 
     * DocumentType from.
     * @return a DocumentType constructed from the given contentType.
     */
    public static DocumentType fromContentType(String contentType) {
        return new ContentTypeDocumentType(contentType);
    }
        
}
