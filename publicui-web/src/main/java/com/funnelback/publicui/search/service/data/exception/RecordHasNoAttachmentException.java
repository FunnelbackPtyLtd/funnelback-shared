package com.funnelback.publicui.search.service.data.exception;

import java.util.regex.Pattern;

/**
 * Exception signaling that a TRIM record doesn't have an attachment
 */
public class RecordHasNoAttachmentException extends TRIMException {

    private static final long serialVersionUID = 1L;

    /** Error message returned by TRIM when a record has no attachments */
    public static final Pattern RECORD_NOT_FOUND = Pattern.compile("Record \\d+ has no document attached");

    /**
     * @param recordUri URI of the TRIM record
     */
    public RecordHasNoAttachmentException(int recordUri) {
        super("Record with URI "+recordUri+" doesn't have an attachment");
    }

}
