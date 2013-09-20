package com.funnelback.publicui.search.web.controllers.content;

import static com.funnelback.publicui.search.web.controllers.content.ContentConstants.CONTENT_DISPOSITION_HEADER;
import static com.funnelback.publicui.search.web.controllers.content.ContentConstants.CONTENT_DISPOSITION_VALUE;
import static com.funnelback.publicui.search.web.controllers.content.ContentConstants.OCTET_STRING_MIME_TYPE;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.log4j.Log4j;

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
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.DataRepository;
import com.funnelback.publicui.search.service.data.exception.AccessToRecordDeniedException;
import com.funnelback.publicui.search.service.data.exception.RecordHasNoAttachmentException;
import com.funnelback.publicui.search.service.data.exception.RecordNotFoundException;
import com.funnelback.publicui.search.service.data.exception.TRIMException;
import com.funnelback.publicui.search.web.controllers.CacheController;

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
    
    @Autowired
    private CacheController cacheController;
    
    @Resource(name="trimReferenceView")
    private View trimReferenceView;
    
    /**
     * @param collectionId TRIM collection to serve a reference from
     * @param trimUri Internal ID of the TRIM record
     * @param noAttachment If set the Content-Disposition header will not be sent
     *  (used for automated testing)
     * @param response HTTP response
     * @return A TRIM <code>.tr5</code> shortcut file pointing
     * to the desired record.
     */
    @RequestMapping(value="/"+DefaultValues.ModernUI.Serve.TRIM_MODERN_LINK_PREFIX+"reference")
    public ModelAndView getTrimReference(
        @RequestParam(value=RequestParameters.COLLECTION) String collectionId,
        @RequestParam(value=RequestParameters.Serve.URI) int trimUri,
        @RequestParam(value=RequestParameters.Click.NOATTACHMENT, required=false, defaultValue="false")
        boolean noAttachment,
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

            if (!noAttachment) {
                response.setContentType(ContentConstants.OCTET_STRING_MIME_TYPE);
                response.addHeader(
                    ContentConstants.CONTENT_DISPOSITION_HEADER,
                    ContentConstants.CONTENT_DISPOSITION_VALUE + "\"search-result-"+Integer.toString(trimUri)+".tr5\"");
            } else {
                response.setContentType(ContentConstants.TEXT_HTML_MIME_TYPE);
            }
                
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
     * @param url URL of the document in the index
     * @param noAttachment If set the Content-Disposition header will not be sent
     *  (used for automated testing)
     * @param request HTTP request
     * @param response HTTP response
     * @return Either the TRIM document will be streamed to the user, or if it's on a
     * record that has no attachments, the cached copy will be returned
     * @throws Exception 
     */
    @RequestMapping(value="/"+DefaultValues.ModernUI.Serve.TRIM_MODERN_LINK_PREFIX+"document")
    public ModelAndView getTrimDocument(
        @RequestParam(value=RequestParameters.COLLECTION) String collectionId,
        @RequestParam(value=RequestParameters.Serve.URI) int trimUri,
        @RequestParam(value=RequestParameters.Cache.URL) String url,
        @RequestParam(value=RequestParameters.Click.NOATTACHMENT, required=false, defaultValue="false")
        boolean noAttachment,
        HttpServletRequest request,
        HttpServletResponse response) throws Exception {
        
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
                
                if (! noAttachment) {
                    response.setContentType(OCTET_STRING_MIME_TYPE);
                    response.addHeader(CONTENT_DISPOSITION_HEADER,
                        CONTENT_DISPOSITION_VALUE+"\""+trimDoc.getName()+"\"");
                } else {
                    response.setContentType(ContentConstants.TEXT_HTML_MIME_TYPE);
                }

                IOUtils.copy(fis, response.getOutputStream());
            } catch (RecordHasNoAttachmentException rhnae) {
                // Access checked passed, but the record doesn't have
                // attachment. Send cached content using the cache controller.
                
                // We can't just redirect to the cache URL here since there's an interceptor
                // that will prevent the request to complete if the collection has DLS enabled.
                log.debug("No attachment for record " + trimUri + ", returning cached copy");
                return cacheController.cache(request, response, collection,
                    DefaultValues.DEFAULT_PROFILE,
                    DefaultValues.DEFAULT_FORM, url,
                    null, 0, -1);
                
            } finally {
                IOUtils.closeQuietly(fis);
                dataRepository.releaseTemporaryTrimDocument(trimDoc);
            }
        }
        
        return null;
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
        if (e instanceof AccessToRecordDeniedException) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getOutputStream().write(i18n.tr("serve.trim.document.access_denied").getBytes());
        } else if (e instanceof RecordNotFoundException) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getOutputStream().write(i18n.tr("serve.trim.document.record_not_found").getBytes());
        } else {
            log.warn("Unknown error while streaming TRIM document", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getOutputStream().write(i18n.tr("serve.trim.document.unknown_error").getBytes());
        }
    }

}
