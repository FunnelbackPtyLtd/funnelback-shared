package com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec;

import lombok.Getter;

/**
 * A padre forking exception that occured because some limit we applied was reached
 * 
 * e.g. a timeout or the response packet size was too large.
 *
 */
public class PadreForkingExceptionPacketSizeTooBig extends PadreForkingException {

    private static final long serialVersionUID = 1L;

    @Getter private long fullPadrePacketSize;    
    
    public PadreForkingExceptionPacketSizeTooBig(String message, long fullPadrePacketSize) {
        super(message);
    }
}
