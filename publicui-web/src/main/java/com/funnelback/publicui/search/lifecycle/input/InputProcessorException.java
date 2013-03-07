package com.funnelback.publicui.search.lifecycle.input;

public class InputProcessorException extends Exception {
    
    private static final long serialVersionUID = 1L;

    public InputProcessorException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public InputProcessorException(String message) {
        super(message);
    }
}
