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
     * Builds a new {@link TRIMException} from an exit code of the <code>GetDocument</code>
     * binary.
     * @param exitCode Exit code of the <code>GetDocumenet</code> binary.
     * @param message the original error message.
     * @param trimUri URI (Unique ID) of the affected record
     * @return A specialized {@link TRIMException} subclass or a {@link TRIMException}
     * if no subclass match the error message.
     */
    public static TRIMException fromGetDocumentExitCode(int exitCode, String message, int trimUri) {
        switch (exitCode) {
        case AccessToRecordDeniedException.ACCESS_DENIED_EXIT_CODE:
            return new AccessToRecordDeniedException(trimUri);
        case RecordNotFoundException.RECORD_NOT_FOUND_EXIT_CODE:
            return new RecordNotFoundException(trimUri);
        case RecordHasNoAttachmentException.RECORD_HAS_NO_ATTACHMENT_EXIT_CODE:
            return new RecordHasNoAttachmentException(trimUri);
        default:
            return new TRIMException(message);
        }

    }

}
