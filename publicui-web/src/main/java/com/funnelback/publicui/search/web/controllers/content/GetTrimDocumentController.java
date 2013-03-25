package com.funnelback.publicui.search.web.controllers.content;

import static com.funnelback.publicui.search.web.controllers.content.ContentConstants.CONTENT_DISPOSITION_HEADER;
import static com.funnelback.publicui.search.web.controllers.content.ContentConstants.CONTENT_DISPOSITION_VALUE;
import static com.funnelback.publicui.search.web.controllers.content.ContentConstants.OCTET_STRING_MIME_TYPE;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.log4j.Log4j;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import com.funnelback.common.config.Collection.Type;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.AbstractPadreForking.EnvironmentKeys;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.DataRepository;
import com.funnelback.publicui.search.service.data.exception.AccessToRecordDeniedException;
import com.funnelback.publicui.search.service.data.exception.RecordNotFoundException;
import com.funnelback.publicui.search.service.data.exception.TRIMException;
import com.funnelback.publicui.utils.ExecutionReturn;
import com.funnelback.publicui.utils.jna.WindowsNativeExecutor;
import com.funnelback.publicui.utils.jna.WindowsFileInputStream.AccessDeniedException;
import com.funnelback.publicui.utils.jna.WindowsNativeExecutor.ExecutionException;

/**
 * Gets a TRIM reference (<code>.tr5</code>) file or stream
 * a document from TRIM to the user.
 *
 */
@Controller
@Log4j
public class GetTrimDocumentController {


    /** Attribute names used for the <code>trim-references</code> template. */
    public static enum ModelAttributes {
        /** TRIM license number */
        trimLicenseNumber,
        /** TRIM dataset identifier */
        trimDatabase,
        /** ID of the record to serve */
        uri;
    };
        
    @Autowired
    private I18n i18n;

    @Autowired
    private ConfigRepository configRepository;
    
    @Autowired
    private DataRepository dataRepository;
    
    @Resource(name="trimReferenceView")
    private View trimReferenceView;
    
    /**
     * @param collectionId TRIM collection to serve a reference from
     * @param trimUri Interal ID of the TRIM record
     * @param response HTTP response
     * @return A TRIM <code>.tr5</code> shortcut file pointing
     * to the desired record.
     */
    @RequestMapping(value="/"+DefaultValues.ModernUI.Serve.TRIM_MODERN_LINK_PREFIX+"reference")
    public ModelAndView getTrimReference(
        @RequestParam(value=RequestParameters.COLLECTION) String collectionId,
        @RequestParam(value=RequestParameters.Serve.URI) int trimUri,
        HttpServletResponse response) {
        
        Collection collection = configRepository.getCollection(collectionId);
        if (collection == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        } else if (! isTrimCollection(collection)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            log.warn("Collection '"+collectionId+"' of type '"+collection.getType()
                 +"' not suitable for serving trim references");
            return null;
        } else {
            
            response.setContentType(ContentConstants.OCTET_STRING_MIME_TYPE);
            response.addHeader(
                ContentConstants.CONTENT_DISPOSITION_HEADER,
                ContentConstants.CONTENT_DISPOSITION_VALUE + "\"search-result-"+Integer.toString(trimUri)+".tr5\"");
                
            ModelAndView mav = new ModelAndView(trimReferenceView);
            mav.getModel().put(
                ModelAttributes.trimLicenseNumber.toString(),
                collection.getConfiguration().value(Keys.Trim.LICENSE_NUMBER));
            mav.getModel().put(
                ModelAttributes.trimDatabase.toString(),
                collection.getConfiguration().value(Keys.Trim.DATABASE));
            mav.getModel().put(ModelAttributes.uri.toString(), Integer.toString(trimUri));
            
            return mav;
        }
    }
    
    /**
     * Stream a TRIM document to the browser
     * @param collectionId TRIM collection
     * @param trimUri URI (Unique ID) of the TRIM record to stream
     * @param response HTTP response
     * @throws IOException 
     * @throws TRIMException 
     */
    @RequestMapping(value="/"+DefaultValues.ModernUI.Serve.TRIM_MODERN_LINK_PREFIX+"document")
    public void getTrimDocument(
        @RequestParam(value=RequestParameters.COLLECTION) String collectionId,
        @RequestParam(value=RequestParameters.Serve.URI) int trimUri,
        HttpServletResponse response) throws IOException, TRIMException {
        
        Collection collection = configRepository.getCollection(collectionId);
        if (collection == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else if (! isTrimCollection(collection)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            log.warn("Collection '"+collectionId+"' of type '"+collection.getType()
                 +"' not suitable for serving trim documents");
        } else {
            File trimDoc = null;
            FileInputStream fis = null;
            try {
                trimDoc = dataRepository.getTemporaryTrimDocument(collection, trimUri);
                fis = new FileInputStream(trimDoc);
                response.setContentType(OCTET_STRING_MIME_TYPE);
                response.addHeader(CONTENT_DISPOSITION_HEADER,
                    CONTENT_DISPOSITION_VALUE+"\""+trimDoc.getName()+"\"");

                IOUtils.copy(fis, response.getOutputStream());
            } finally {
                IOUtils.closeQuietly(fis);
                dataRepository.releaseTemporaryTrimDocument(trimDoc);
            }
        }
        
    }
    
    /**
     * Check if the collection may contain TRIM document, i.e. if it's a
     * {@link Type#trim}, {@link Type#trimpush} or {@link Type#push} one.
     * @param c Collection to check
     * @return true if this collection might contain TRIM documents, false otherwise.
     */
    private boolean isTrimCollection(Collection c) {
        return Type.trim.equals(c.getType())
            || Type.trimpush.equals(c.getType())
            || Type.push.equals(c.getType());
    }
    
    @ExceptionHandler(TRIMException.class)
    private void exceptionHandler(TRIMException e, HttpServletResponse response)
        throws IOException {
        response.setContentType("text/plain");
        log.error("Error while streaming TRIM document", e);
        if (e instanceof AccessToRecordDeniedException) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getOutputStream().write(i18n.tr("serve.trim.document.access_denied").getBytes());
        } else if (e instanceof RecordNotFoundException) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getOutputStream().write(i18n.tr("serve.trim.document.record_not_found").getBytes());
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getOutputStream().write(i18n.tr("serve.trim.document.unknown_error").getBytes());
        }
    }

}
