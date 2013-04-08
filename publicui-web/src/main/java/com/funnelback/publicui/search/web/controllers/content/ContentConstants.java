package com.funnelback.publicui.search.web.controllers.content;

/**
 * Constants for controllers serving content
 */
public class ContentConstants {
    
    /** Content-Disposition header name */
    public static final String CONTENT_DISPOSITION_HEADER = "Content-Disposition";
    
    /** Value for the Content-Disposition header, to be completed with the filename */
    public static final String CONTENT_DISPOSITION_VALUE = "attachment; filename=";
    
    /** Content type when serving files */
    public static final String OCTET_STRING_MIME_TYPE = "application/octet-stream";
    
    /** Content type when serving a partial file when <code>noattachment=1</code> is used */
    public static final String TEXT_HTML_MIME_TYPE = "text/html";
    
    /** text/plain content type */
    public static final String TEXT_PLAIN_MIME_TYPE = "text/plain";

}
