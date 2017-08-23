package com.funnelback.publicui.search.web.exception;

/**
 * Exception representing an error due to procesing a collection which is in some way invalid.
 * 
 * e.g. We tried to read a collection which has no _default profile.
 */
public class InvalidCollectionException extends Exception {

    public InvalidCollectionException(String message) {
        super(message);
    }

    public InvalidCollectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
