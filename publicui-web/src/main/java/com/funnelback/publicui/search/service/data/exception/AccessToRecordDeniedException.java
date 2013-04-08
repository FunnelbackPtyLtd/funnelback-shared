package com.funnelback.publicui.search.service.data.exception;

/**
 * Exception signaling that the access to a TRIM record was denied
 * to a specific user.
 *
 */
public class AccessToRecordDeniedException extends TRIMException {

    private static final long serialVersionUID = 1L;
    
    /** Exit code returned when the access is denied */
    public static final int ACCESS_DENIED_EXIT_CODE = 1;
    
    /**
     * @param recordUri URI of the TRIM record
     */
    public AccessToRecordDeniedException(int recordUri) {
        super("Access to record with URI "+recordUri+" denied");
    }
    
}
