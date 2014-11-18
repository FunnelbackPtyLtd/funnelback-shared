package com.funnelback.publicui.search.service.data.exception;

/**
 * Exception signaling that a TRIM record doesn't have an attachment
 */
public class RecordHasNoAttachmentException extends TRIMException {

    private static final long serialVersionUID = 1L;

    /** Exit code returned when a record has no attachments */
    public static final int RECORD_HAS_NO_ATTACHMENT_EXIT_CODE = 3;

    /**
     * @param recordUri URI of the TRIM record
     */
    public RecordHasNoAttachmentException(int recordUri) {
        super("Record with URI "+recordUri+" doesn't have an attachment");
    }

}
