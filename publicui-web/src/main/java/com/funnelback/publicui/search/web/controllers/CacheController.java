package com.funnelback.publicui.search.web.controllers;

import groovy.lang.Script;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

import org.apache.http.HttpResponse;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.common.Xml;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.common.io.store.RawBytesRecord;
import com.funnelback.common.io.store.Record;
import com.funnelback.common.io.store.Store;
import com.funnelback.common.io.store.Store.RecordAndMetadata;
import com.funnelback.common.io.store.Store.View;
import com.funnelback.common.io.store.XmlRecord;
import com.funnelback.publicui.search.lifecycle.GenericHookScriptRunner;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Collection.Hook;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.DataRepository;
import com.funnelback.publicui.search.web.binding.CollectionEditor;

/**
 * Deal with cached copies
 */
@Controller
@Log4j
public class CacheController {

    /** Default FTL file to use when cached copies are not available */
    public static final String CACHED_COPY_UNAVAILABLE_VIEW = DefaultValues.FOLDER_WEB+"/"
            + DefaultValues.FOLDER_TEMPLATES + "/"
            + DefaultValues.FOLDER_MODERNUI + "/cached-copy-unavailable";
    
    /** Model attribute containing document's metadata */
    public final static String MODEL_METADATA = "metaData";
    
    /** Model attribute containing the Jsoup document tree */
    public final static String MODEL_DOCUMENT = "doc";
    
    /** Model attribute containing the request URL, needed to build an URL to the Funnelback server */
    public final static String MODEL_REQUEST_URL = "requestURL";
    
    /** Default content type for non-XML records when it's not present in the document metadata */
    public final static String DEFAULT_CONTENT_TYPE = "text/html";
    
    /** Default content type for XML records */
    public final static String DEFAULT_XML_CONTENT_TYPE = "text/xml";
    
    /** Default charset for non-XML records when it's not present in the document metadata */
    public final static String DEFAULT_CHARSET = "UTF-8";
    
    /** Used for XSL transformations */
    private final TransformerFactory transformerFactory = TransformerFactory.newInstance();
    
    @Autowired
    @Setter private DataRepository dataRepository;
    
    @Autowired
    @Setter private ConfigRepository configRepository;
    
    @InitBinder
    public void initBinder(DataBinder binder) {
        binder.registerCustomEditor(Collection.class, new CollectionEditor(configRepository));
    }

    @RequestMapping(value="/cache", method=RequestMethod.GET)
    public ModelAndView cache(HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value=RequestParameters.COLLECTION,required=true) Collection collection,
            @RequestParam(defaultValue=DefaultValues.DEFAULT_PROFILE) String profile,
            @RequestParam(defaultValue=DefaultValues.DEFAULT_FORM) String form,
            @RequestParam(required=true) String url) throws Exception {
        
        if (url == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } else if (collection.getConfiguration().valueAsBoolean(Keys.UI_CACHE_DISABLED)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        } else {
            RecordAndMetadata<? extends Record<?>> rmd = dataRepository.getCachedDocument(collection, View.live, url);
                
            if (rmd != null && rmd.record != null) {
                if (runHookScript(collection, Hook.pre_cache, rmd)) {
                    if (rmd.record instanceof RawBytesRecord) {
                        RawBytesRecord bytesRecord = (RawBytesRecord) rmd.record;
                        
                        Map<String, Object> model = new HashMap<String, Object>();
                        model.put(RequestParameters.Cache.URL, url);
                        model.put(RequestParameters.COLLECTION, collection);
                        model.put(RequestParameters.PROFILE, profile);
                        model.put(RequestParameters.FORM, form);
                        model.put(MODEL_REQUEST_URL, new URL(request.getRequestURL().toString()));
                        model.put(MODEL_METADATA, rmd.metadata);
                        model.put(SearchController.ModelAttributes.httpRequest.toString(), request);
                        
                        String charset = setContentTypeAndCharset(response, rmd.metadata);
                        model.put(MODEL_DOCUMENT, Jsoup.parse(new String(bytesRecord.getContent(),charset)));
                        
                        String view = DefaultValues.FOLDER_CONF
                                + "/" + collection.getId()
                                + "/" + profile
                                + "/" + form + DefaultValues.CACHE_FORM_SUFFIX;
                        
                        return new ModelAndView(view, model);
                        
                    } else if (rmd.record instanceof XmlRecord) {
                        XmlRecord xmlRecord = (XmlRecord) rmd.record;
    
                        // Set custom content-type if any
                        response.setContentType(
                                collection.getConfiguration().value(
                                        Keys.ModernUI.Cache.FORM_PREFIX
                                        + "." + form + "." + Keys.ModernUI.FORM_CONTENT_TYPE_SUFFIX,
                                DEFAULT_XML_CONTENT_TYPE));
                        
                        // XSL Transform if there's an XSL template
                        File xslTemplate = configRepository.getXslTemplate(collection.getId(), profile);
                        if (xslTemplate != null) {
                            Transformer transformer = transformerFactory.newTransformer(new StreamSource(xslTemplate));
                            DOMSource xmlSource = new DOMSource(xmlRecord.getContent());
                            transformer.transform(xmlSource, new StreamResult(response.getOutputStream()));
                        } else {
                            response.getWriter().write(Xml.toString(xmlRecord.getContent()));
                        }
                        
                        return null;
                    } else {
                        throw new UnsupportedOperationException("Unknown record type '"+rmd.record.getClass()+"'");
                    }
                } else {
                    // Disabled by hook script
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                }
            } else {
                // Record not found
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        }
        
        return new ModelAndView(CACHED_COPY_UNAVAILABLE_VIEW, new HashMap<String, Object>());
    }
    
    /**
     * Sets the content type on the HTTP response and return the charset
     * @param response {@link HttpResponse} to set the header on
     * @param headers List of headers for the content, from the store
     * @return The charset for the document, defaulting to UTF-8 is not found
     */
    private String setContentTypeAndCharset(HttpServletResponse response, Map<String, String> headers) {
        String contentType = DEFAULT_CONTENT_TYPE;
        String charset = DEFAULT_CHARSET;
        if (headers.containsKey(Store.CONTENT_TYPE)) {
            contentType = headers.get(Store.CONTENT_TYPE);
        }
        
        if (!contentType.contains(Store.CHARSET) && headers.containsKey(Store.Header.Charset.toString())) {
            charset = headers.get(Store.Header.Charset.toString());
            contentType += ";"+Store.CHARSET+"=" + charset;
        }
        
        response.setContentType(contentType);
        
        return charset;
    }
    
    /**
     * Runs a cache hook script
     * @param c Collection to get the hook script from
     * @param hook Hook to run
     * @return Value returned by the hook script if it's a boolean, true otherwise. Also returns
     * true if there's no hook script to run
     */
    private boolean runHookScript(Collection c, Hook hook, RecordAndMetadata<? extends Record<?>> rmd) {
        Class<Script> hookScriptClass = c.getHookScriptsClasses().get(hook);
        if (hookScriptClass != null) {
            try {
                Map<String, Object> data = new HashMap<>();
                data.put(Hook.COLLECTION_KEY, c);
                data.put(Hook.DOCUMENT_KEY, rmd.record);
                data.put(Hook.METADATA_KEY, rmd.metadata);
                Object value = GenericHookScriptRunner.runScript(hookScriptClass, data);
                if (value != null && value instanceof Boolean) {
                    return (Boolean) value;
                }
            } catch (Throwable t) {
                log.error("Error while running " + hook.toString() + " hook for collection '" + c.getId() + "'", t);
            }
        }
        
        return true;
    }
    
}
