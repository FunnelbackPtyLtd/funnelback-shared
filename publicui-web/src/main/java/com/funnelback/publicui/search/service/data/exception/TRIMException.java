package com.funnelback.publicui.search.service.data.exception;

/**
 * TRIM related exceptions
 */
public class TRIMException extends Exception {

    private static final long serialVersionUID = 1L;
    
    /**
     * @param message Message of the exception
     */
    public TRIMException(String message) {
        super(message);
    }
    
    /**
     * @param cause Nested cause
     */
    public TRIMException(Throwable cause) {
        super(cause);
    }
    
    /**
     * Builds a new {@link TRIMException} from a TRIM error message
     * @param trimMsg The TRIM error message
     * @param trimUri URI (Unique ID) of the affected record
     * @return A specialized {@link TRIMException} subclass or a {@link TRIMException}
     * if no subclass match the error message.
     */
    public static TRIMException fromTRIMMessage(String trimMsg, int trimUri) {
        if (trimMsg != null && AccessToRecordDeniedException.ACCESS_DENIED.equals(trimMsg.trim())) {
            return new AccessToRecordDeniedException(trimUri);
        } else if (trimMsg != null && RecordNotFoundException.RECORD_NOT_FOUND.equals(trimMsg.trim())) {
            return new RecordNotFoundException(trimUri);
        } else if (trimMsg != null
            && RecordHasNoAttachmentException.RECORD_NOT_FOUND.matcher(trimMsg).matches()) {
            return new RecordHasNoAttachmentException(trimUri);
        } else {
            return new TRIMException(trimMsg);
        }

    }

}
