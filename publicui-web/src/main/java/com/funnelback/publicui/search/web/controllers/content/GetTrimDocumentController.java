package com.funnelback.publicui.search.web.controllers.content;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.log4j.Log4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import com.funnelback.common.config.Collection.Type;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.service.ConfigRepository;

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
    private ConfigRepository configRepository;
    
    @Resource(name="trimReferenceView")
    private View trimReferenceView;
    
    /**
     * @param collectionId TRIM collection to serve a reference from
     * @param trimUri Interal ID of the TRIM record
     * @param response HTTP response
     * @return A TRIM <code>.tr5</code> shortcut file pointing
     * to the desired record.
     */
    @RequestMapping(value="/trim.reference")
    public ModelAndView getTrimReference(
        @RequestParam(value="collection") String collectionId,
        @RequestParam(value="uri") int trimUri,
        HttpServletResponse response) {
        
        Collection collection = configRepository.getCollection(collectionId);
        if (collection == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        } else if (! Type.trim.equals(collection.getType())
            && ! Type.push.equals(collection.getType())) {
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
}
