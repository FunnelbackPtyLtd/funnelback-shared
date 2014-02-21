package com.funnelback.publicui.search.web.controllers;

import com.codahale.metrics.MetricRegistry;
import com.funnelback.common.StoreView;
import com.funnelback.common.View;
import com.funnelback.common.Xml;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.common.io.store.*;
import com.funnelback.common.io.store.Store.RecordAndMetadata;
import com.funnelback.publicui.search.lifecycle.GenericHookScriptRunner;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Collection.Hook;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.DataRepository;
import com.funnelback.publicui.search.web.binding.CollectionEditor;
import com.funnelback.publicui.search.web.binding.RelativeFileOnlyEditor;
import com.funnelback.publicui.utils.web.MetricsConfiguration;
import groovy.lang.Script;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

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

    @Autowired
    @Setter private MetricRegistry metrics;

    @InitBinder
    public void initBinder(DataBinder binder) {
        binder.registerCustomEditor(Collection.class, new CollectionEditor(configRepository));
        binder.registerCustomEditor(File.class, new RelativeFileOnlyEditor());
    }

    /**
     * Process cache requests
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @param collection Collection to get a cached copy from
     * @param profile Profile to get a cached copy for
     * @param form Cache form to use
     * @param url URL of the document to get a cached copy of
     * @param doc Relative path of the document from the collection live data folder
     * @param offset to read the content from
     * @param length Length of content to read
     * @return {@link ModelAndView}
     * @throws Exception 
     */
    @RequestMapping(value="/cache", method=RequestMethod.GET)
    public ModelAndView cache(HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value=RequestParameters.COLLECTION, required=true) Collection collection,
            @RequestParam(defaultValue=DefaultValues.DEFAULT_PROFILE) String profile,
            @RequestParam(defaultValue=DefaultValues.DEFAULT_FORM) String form,
            @RequestParam(required=true) String url,
            @RequestParam File doc,
            @RequestParam(value=RequestParameters.Cache.OFFSET, defaultValue="0") int offset,
            @RequestParam(value=RequestParameters.Cache.LENGTH, defaultValue="-1") int length) throws Exception {
        
        if (url == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } else if (collection.getConfiguration().valueAsBoolean(Keys.UI_CACHE_DISABLED)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        } else {
            RecordAndMetadata<? extends Record<?>> rmd
                    = dataRepository.getCachedDocument(collection, StoreView.live, url);
            if ((rmd == null || rmd.record == null) && doc != null) {
                // Attempt with DOC parameter
                rmd = dataRepository.getDocument(collection, StoreView.live, url, doc, offset, length);
            }
                
            if (rmd != null && rmd.record != null) {
                HookScriptResult hookResult = runHookScript(collection, Hook.pre_cache, rmd);
                if (hookResult.authorized) {
                    if (hookResult.record != null && hookResult.metadata != null) {
                        rmd = new RecordAndMetadata<Record<?>>(hookResult.record, hookResult.metadata);
                    }
                    
                    if (rmd.record instanceof RawBytesRecord || rmd.record instanceof StringRecord) {
                        String content = getContent(rmd.record);
                        
                        Map<String, Object> model = new HashMap<String, Object>();
                        model.put(RequestParameters.Cache.URL, url);
                        model.put(RequestParameters.COLLECTION, collection);
                        model.put(RequestParameters.PROFILE, profile);
                        model.put(RequestParameters.FORM, form);
                        model.put(MODEL_REQUEST_URL, new URL(request.getRequestURL().toString()));
                        model.put(MODEL_METADATA, rmd.metadata);
                        model.put(SearchController.ModelAttributes.httpRequest.toString(), request);
                        
                        String charset = getCharset(rmd.metadata);
                        model.put(MODEL_DOCUMENT, Jsoup.parse(content, charset));
                        
                        String view = DefaultValues.FOLDER_CONF
                                + "/" + collection.getId()
                                + "/" + profile
                                + "/" + form + DefaultValues.CACHE_FORM_SUFFIX;

                        incrementMetrics(collection, profile);
                        
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

                        incrementMetrics(collection, profile);
                        
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
                log.debug("Cached copy of '"+url+"' not found on collection '"+collection.getId()+"'");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        }
        
        return new ModelAndView(CACHED_COPY_UNAVAILABLE_VIEW, new HashMap<String, Object>());
    }
    
    /**
     * @param headers List of headers for the content, from the store
     * @return The charset for the document, defaulting to UTF-8 is not found
     */
    private String getCharset(Map<String, String> headers) {
        if (headers.containsKey(Store.Header.Charset.toString())) {
            return headers.get(Store.Header.Charset.toString());
        } else {
            return DEFAULT_CHARSET;
        }
    }
    
    /**
     * Get the content of a record as a String, using the default
     * charset.
     * @param r The record to get the content from
     * @return The content as a String
     */
    private String getContent(Record<?> r) {
        if (r instanceof RawBytesRecord) {
            return new String(((RawBytesRecord) r).getContent());
        } else if (r instanceof StringRecord) {
            return ((StringRecord) r).getContent();
        } else {
            throw new IllegalArgumentException("Unsupported record type " + r.getClass().getName());
        }
    }
    
    /**
     * Runs a cache hook script
     * @param c Collection to get the hook script from
     * @param hook Hook to run
     * @return Value returned by the hook script if it's a boolean, true otherwise. Also returns
     * true if there's no hook script to run
     */
    private HookScriptResult runHookScript(Collection c, Hook hook, RecordAndMetadata<? extends Record<?>> rmd) {
        Class<Script> hookScriptClass = c.getHookScriptsClasses().get(hook);
        if (hookScriptClass != null) {
            try {
                Map<String, Object> data = new HashMap<>();
                data.put(Hook.COLLECTION_KEY, c);
                data.put(Hook.DOCUMENT_KEY, rmd);
                Object value = GenericHookScriptRunner.runScript(hookScriptClass, data);
                if (value != null && value instanceof Boolean) {
                    return new HookScriptResult((boolean) value, rmd.record, rmd.metadata);
                } else {
                    return new HookScriptResult(true, rmd.record, rmd.metadata);
                }
            } catch (Throwable t) {
                log.error("Error while running " + hook.toString() + " hook for collection '" + c.getId() + "'", t);
            }
        }
        
        return new HookScriptResult(true, null, null);
    }
    
    /**
     * Increment the cache controller hit metrics
     * @param collection Collection
     * @param profile Profile ID
     */
    private void incrementMetrics(Collection collection, String profile) {
        metrics.counter(MetricRegistry.name(
            MetricsConfiguration.ALL_NS, MetricsConfiguration.CACHE)).inc();

        metrics.counter(MetricRegistry.name(
            MetricsConfiguration.COLLECTION_NS, collection.getId(),
            profile, MetricsConfiguration.CACHE)).inc();        
    }
    
    @RequiredArgsConstructor
    private static class HookScriptResult {
        public final boolean authorized;
        public final Record<?> record;
        public final Map<String, String> metadata;
    }
    
    /**
     * Display an error message is something went wrong
     * @param response HTTP Response, to set the status code to 500
     * @param e Exception that occured
     * @return {@link ModelAndView}
     */
    @ExceptionHandler
    public ModelAndView handleException(HttpServletResponse response, Exception e) {
        log.error("An error occured while processing a cache request", e);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.setContentType("text/html");
        return new ModelAndView(CACHED_COPY_UNAVAILABLE_VIEW, "exception", e);
    }
    
}
