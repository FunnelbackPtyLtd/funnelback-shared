package com.funnelback.publicui.search.service.data.exception;

/**
 * Exception signaling that a TRIM record could not be found.
 */
public class RecordNotFoundException extends TRIMException {

    private static final long serialVersionUID = 1L;
    
    /** Exit code returned when a record is not found */
    public static final int RECORD_NOT_FOUND_EXIT_CODE = 2;

    /**
     * @param recordUri URI (Unique ID) of the TRIM record
     */
    public RecordNotFoundException(int recordUri) {
        super("Record with URI "+recordUri+" not found");
    }
    
}
