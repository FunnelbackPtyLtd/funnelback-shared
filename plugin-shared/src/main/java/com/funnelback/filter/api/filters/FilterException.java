package com.funnelback.filter.api.filters;

import java.net.URI;

import com.funnelback.filter.api.documents.FilterableDocument;

/**
 * Thrown when an error occurs during filtering of a document and filtering of that document should not proceed.
 *
 */
public class FilterException extends RuntimeException {

    
    private static final long serialVersionUID = 1L;
    
    private final URI documentURI;
    
    public FilterException(FilterableDocument documentBeingFiltered, String message) {
        super(message);
        documentURI = documentBeingFiltered.getURI();
    }
    
    public FilterException(FilterableDocument documentBeingFiltered, String message, Throwable cause) {
        super(message, cause);
        documentURI = documentBeingFiltered.getURI();
    }
    
    @Override
    public String getMessage() {
        return "Failed to filter document: '" + documentURI.toASCIIString() + "', failure message is: " 
                    + super.getMessage();
    }
}
