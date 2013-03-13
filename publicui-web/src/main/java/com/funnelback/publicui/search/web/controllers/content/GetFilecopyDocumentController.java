package com.funnelback.publicui.search.web.controllers.content;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.log4j.Log4j;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.funnelback.common.config.Collection.Type;
import com.funnelback.common.config.Config;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.DataRepository;
import com.funnelback.publicui.search.service.data.filecopy.WindowsNativeInputStream.AccessDeniedException;

/**
 * Controller in charge of serving files from Filecopy collection
 * to users.
 * 
 * @since 12.4
 */
@Controller
@Log4j
public class GetFilecopyDocumentController {

    private static final String CONTENT_DISPOSITION_HEADER = "Content-Disposition";
    private static final String CONTENT_DISPOSITION_VALUE = "attachment; filename=";
    private static final String OCTET_STRING_MIME_TYPE = "application/octet-stream";
    private static final String TEXT_HTML_MIME_TYPE = "text/html";
    
    private static final String UNKNOWN_FILE_NAME = "unknown.file";
    
    @Autowired
    private I18n i18n;
    
    /**
     * Only stream 2k of data in &quot;noattachment&quot; mode, as used in the
     * automated tests.
     */
    private static final int NOATTACHMENT_LIMIT = 2048;
    
    @Autowired
    private ConfigRepository configRepository;
    
    @Autowired
    private DataRepository dataRepository;
    
    /**
     * Fetches a document from a fileshare, for a filecopy collection
     * @param collectionId ID of the filecopy collection
     * @param uri URI of the document (as of in the index)
     * @param noAttachment Whether to send or not the "Content-Disposition" header
     * @param response HTTP response
     * @param request HTTP request
     * @throws IOException If something goes wrong
     */
    @RequestMapping(value="/filecopy.document",
            params={RequestParameters.COLLECTION, RequestParameters.Serve.URI})
    public void getFilecopyDocument(
            @RequestParam("collection") String collectionId,
            @RequestParam("uri") URI uri,
            @RequestParam(value="noattachment", required=false, defaultValue="false") boolean noAttachment,
            HttpServletResponse response,
            HttpServletRequest request) throws IOException {
        
        Collection collection = configRepository.getCollection(collectionId);
        if (collection == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        } else if (! Type.filecopy.equals(collection.getType())) {
            log.warn("Collection '"+collectionId+"' of type '"+collection.getType()
                 +"' not suitable for serving filecopy documents");
            return;
        } else {
            
            // If DLS is on, fetch the file natively as the impersonated user
            String dlsMode = collection.getConfiguration()
                .value(Keys.DocumentLevelSecurity.DOCUMENT_LEVEL_SECURITY_MODE);
            String dlsAction = collection.getConfiguration()
                .value(Keys.DocumentLevelSecurity.DOCUMENT_LEVEL_SECURITY_ACTION);
            String securityModel = collection.getConfiguration()
                .value(Keys.FileCopy.SECURITY_MODEL);
            
            if ((DefaultValues.DocumentLevelSecurity.ACTION_NTFS.equals(dlsAction)
                    && ! Config.isFalse(dlsMode))
                    || (securityModel != null && !DefaultValues.FileCopy.SECURITY_MODEL_NONE.equals(securityModel))) {
                
                // DLS mode, ensure impersonation is enabled
                if (! collection.getConfiguration().valueAsBoolean(Keys.ModernUI.AUTHENTICATION)
                        || request.getUserPrincipal() == null) {
                    log.error("DLS is enabled on collection '"+collectionId+"' but the request is not impersonated."
                        + " Ensure "+Keys.ModernUI.AUTHENTICATION
                        +" is enabled and that Windows authentication is working.");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            }
  
            try (InputStream is = dataRepository.getFilecopyDocument(collection, uri, true)) {
                if (noAttachment) {
                    response.setContentType(TEXT_HTML_MIME_TYPE);
                    
                    // Only send the first 2 KBs
                    byte[] b = new byte[NOATTACHMENT_LIMIT];
                    int nbRead = new BufferedInputStream(is, NOATTACHMENT_LIMIT).read(b);
                    response.getOutputStream().write(b, 0, nbRead);
                } else {
                    response.setContentType(OCTET_STRING_MIME_TYPE);
                    response.addHeader(CONTENT_DISPOSITION_HEADER,
                        CONTENT_DISPOSITION_VALUE+"\""+getFilename(uri)+"\"");
                    IOUtils.copy(is, response.getOutputStream());
                }
            }
        }
        
    }
    
    private String getFilename(URI uri) {
        if (uri.getPath() != null) {
            if (! uri.getPath().endsWith("/") && uri.getPath().lastIndexOf('/') > -1) {
                return uri.getPath().substring(uri.getPath().lastIndexOf('/')+1);
            }
        }
        
        return UNKNOWN_FILE_NAME;
    }
    
    @ExceptionHandler(IOException.class)
    private void exceptionHandler(IOException e, HttpServletResponse response)
        throws IOException {
        response.setContentType("text/plain");
        log.error("I/O error while streaming file", e);
        if (e instanceof AccessDeniedException) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getOutputStream().write(i18n.tr("serve.filecopy.access_denied").getBytes());
        } else if (e instanceof FileNotFoundException) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getOutputStream().write(i18n.tr("serve.filecopy.file_not_found").getBytes());
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getOutputStream().write(i18n.tr("serve.filecopy.unknown_error").getBytes());
        }
    }
    
}
