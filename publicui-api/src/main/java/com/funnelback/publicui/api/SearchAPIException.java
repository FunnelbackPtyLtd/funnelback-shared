package com.funnelback.publicui.api;

/**
 * Generic exception for Search API.
 * 
 * This exception denotes a problem in using the Search API,
 * not in the underlying search service.
 */
public class SearchAPIException extends Exception {

    private static final long serialVersionUID = 1L;
    
    public SearchAPIException(String message) {
        super(message);
    }
    
    public SearchAPIException(Throwable cause) {
        super(cause);
    }

}
