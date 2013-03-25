package com.funnelback.publicui.search.service.data.exception;

/**
 * Exception signaling that the access to a TRIM record was denied
 * to a specific user.
 *
 */
public class AccessToRecordDeniedException extends TRIMException {

    private static final long serialVersionUID = 1L;
    
    /** Message returned by TRIM when the access is denied */
    public static final String ACCESS_DENIED = "Access denied";
    
    /**
     * @param recordUri URI of the TRIM record
     */
    public AccessToRecordDeniedException(int recordUri) {
        super("Access to record with URI '"+recordUri+" denied");
    }
    
}
