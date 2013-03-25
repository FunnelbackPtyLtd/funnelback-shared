package com.funnelback.publicui.search.service.data.exception;

/**
 * Exception signaling that a TRIM record could not be found.
 */
public class RecordNotFoundException extends TRIMException {

    private static final long serialVersionUID = 1L;
    
    /** Error message returned by TRIM when a record is not found */
    public static final String RECORD_NOT_FOUND = "An item with this identifier (URI) could not be found.";

    /**
     * @param recordUri URI (Unique ID) of the TRIM record
     */
    public RecordNotFoundException(int recordUri) {
        super("Record with URI '"+recordUri+"' not found");
    }
    
}
