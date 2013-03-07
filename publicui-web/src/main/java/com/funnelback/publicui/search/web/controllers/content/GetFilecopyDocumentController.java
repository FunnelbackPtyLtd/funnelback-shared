package com.funnelback.publicui.search.web.controllers.content;

import java.io.IOException;
import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.log4j.Log4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.funnelback.common.config.Collection.Type;
import com.funnelback.common.config.Config;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.DataRepository;

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
    
    /**
     * Only stream 2k of data in &quot;noattachment&quot; mode, as used in the
     * automated tests.
     */
    private static final int NOATTACHMENT_LIMIT = 2048;
    
    @Autowired
    private ConfigRepository configRepository;
    
    @Autowired
    private DataRepository dataRepository;
    
    @RequestMapping(value="/filecopy.document",
            params={RequestParameters.COLLECTION, RequestParameters.Serve.URI})
    public void getFilecopyDocument(
            @RequestParam("collection") String collectionId,
            @RequestParam("uri") URI uri,
            @RequestParam("noattachment") boolean noAttachment,
            HttpServletResponse response,
            HttpServletRequest request) throws IOException {
        
        Collection collection = configRepository.getCollection(collectionId);
        if (collection == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        } else if (! Type.filecopy.equals(collection.getType())) {
            log.warn("Collection '"+collectionId+"' of type '"+collection.getType()+"' not suitable for serving filecopy documents");
            return;
        } else {
            
            // If DLS is on, fetch the file natively as the impersonated user
            String dlsMode = collection.getConfiguration().value(Keys.DocumentLevelSecurity.DOCUMENT_LEVEL_SECURITY_MODE);
            String dlsAction = collection.getConfiguration().value(Keys.DocumentLevelSecurity.DOCUMENT_LEVEL_SECURITY_ACTION);
            
            if ((DefaultValues.DocumentLevelSecurity.ACTION_NTFS.equals(dlsAction)
                    && ! Config.isFalse(dlsMode))
                    || collection.getConfiguration().hasValue(Keys.FileCopy.SECURITY_MODEL)) {
                // DLS mode.
                // Ensure impersonation is enabled
                
                if (! collection.getConfiguration().valueAsBoolean(Keys.ModernUI.AUTHENTICATION)
                        || request.getUserPrincipal() == null) {
                    log.error("DLS is enabled on collection '"+collectionId+"' but the request is not impersonated."
                            + " Ensure "+Keys.ModernUI.AUTHENTICATION+" is enabled and that Windows authentication is working.");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
                
                if (noAttachment) {
                    response.setContentType(TEXT_HTML_MIME_TYPE);
                    dataRepository.streamFilecopyDocument(collection, uri, true, response.getOutputStream(), NOATTACHMENT_LIMIT);
                } else {
                    response.setContentType(OCTET_STRING_MIME_TYPE);
                    response.addHeader(CONTENT_DISPOSITION_HEADER, CONTENT_DISPOSITION_VALUE+getFilename(uri));
                    dataRepository.streamFilecopyDocument(collection, uri, true, response.getOutputStream());
                }

                
                
            } else {
                // Non DLS mode
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
    
}
