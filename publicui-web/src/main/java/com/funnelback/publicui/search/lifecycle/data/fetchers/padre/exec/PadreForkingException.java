package com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec;


public class PadreForkingException extends Exception {

    private static final long serialVersionUID = 1L;

    public PadreForkingException(Throwable cause) {
        super(cause);
    }
    
    public PadreForkingException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public PadreForkingException(String message) {
        super(message);
    }
}
