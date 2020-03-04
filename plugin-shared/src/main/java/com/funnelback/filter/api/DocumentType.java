package com.funnelback.filter.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

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
    public static final DocumentType MIME_HTML_TEXT = _DocumentType.builder().HTML(true).contentType("text/html").build();
    
    /**
     * Simple json MIME type which should be used when a filter converts a document to json.
     */
    public static final DocumentType MIME_APPLICATION_JSON_TEXT = _DocumentType.builder().JSON(true).contentType("application/json").build();
    
    /**
     * Simple json MIME type which should be used when a filter converts a document to json.
     */
    @Deprecated
    public static final DocumentType APPLICATION_JSON_TEXT = MIME_APPLICATION_JSON_TEXT;

    /**
     * Simple xml MIME type which should be used when a filter converts a document to xml.
     */
    public static final DocumentType MIME_XML_TEXT = _DocumentType.builder().XML(true).contentType("text/xml").build();
    
    /**
     * Standard CSV MIME type which should be used when a filter converts a document to CSV
     */
    public static final DocumentType MIME_CSV_TEXT = _DocumentType.builder().contentType("text/csv").build();

    /**
     * Deprecated do not use, use MIME_XHTML instead.
     */
    @Deprecated
    public static final DocumentType MIME_XHTML_TEXT = _DocumentType.builder().HTML(true).XML(true).contentType("text/xhtml").build();
    
    /**
     * Simple xhtml MIME type which should be used when a filter converts a document to xhtml.
     */
    public static final DocumentType MIME_XHTML_XML = _DocumentType.builder().HTML(true).XML(true).contentType("application/xhtml+xml").build();

    /**
     * A mime type that is unknown. Filters may be written to detect this and replace the 
     * mime type with something better. 
     */
    public static final DocumentType MIME_UNKNOWN = _DocumentType.builder().contentType("application/octet-stream").build();
    
    /**
     * A document type for plain text documents.
     */
    public static final DocumentType MIME_TEXT_PLAIN = _DocumentType.builder().contentType("text/plain").build();

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
    
    @Builder(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class _DocumentType implements DocumentType {

        @Builder.Default @Getter public final boolean HTML = false;
        @Builder.Default @Getter public final boolean XML = false;
        @Builder.Default @Getter public final boolean JSON = false;
        public final String contentType;

        @Override
        public String asContentType() {
            return contentType;
        }
    }
}
