package com.funnelback.publicui.search.web.controllers;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.codahale.metrics.MetricRegistry;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.common.io.store.RawBytesRecord;
import com.funnelback.common.io.store.Record;
import com.funnelback.common.io.store.Store;
import com.funnelback.common.io.store.Store.RecordAndMetadata;
import com.funnelback.common.io.store.StringRecord;
import com.funnelback.common.io.store.XmlRecord;
import com.funnelback.common.io.store.record.ConvertRecordType;
import com.funnelback.common.utils.XMLUtils;
import com.funnelback.common.views.StoreView;
import com.funnelback.publicui.search.lifecycle.GenericHookScriptRunner;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Collection.Hook;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.cache.CacheQuestion;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.DataRepository;
import com.funnelback.publicui.search.service.security.DLSEnabledChecker;
import com.funnelback.publicui.search.web.binding.CollectionEditor;
import com.funnelback.publicui.search.web.binding.StringArrayFirstSlotEditor;
import com.funnelback.publicui.utils.web.MetricsConfiguration;
import com.funnelback.springmvc.web.binder.RelativeFileOnlyEditor;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;

import groovy.lang.Script;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * Deal with cached copies
 */
@Controller
@Log4j2
public class CacheController {

    /** Default FTL file to use when cached copies are not available */
    public static final String CACHED_COPY_UNAVAILABLE_VIEW = DefaultValues.FOLDER_WEB+"/"
            + DefaultValues.FOLDER_TEMPLATES + "/"
            + DefaultValues.FOLDER_MODERNUI + "/cached-copy-unavailable";
    
    /** Model attribute containing document's metadata */
    @Deprecated
    public final static String MODEL_METADATA_V1 = "metaData";
    
    /** Model attribute containing document's metadata as a Map<String, Collection<String>> */
    public final static String MODEL_METADATA_V2 = "metaDataV2";
    
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
    
    @Autowired
    @Setter private DLSEnabledChecker dLSEnabledChecker;

    @InitBinder
    public void initBinder(DataBinder binder) {
        // For security reasons, only allow specific fields for
        // data binding
        binder.setAllowedFields(
                RequestParameters.COLLECTION,
                RequestParameters.PROFILE,
                RequestParameters.FORM,
                RequestParameters.Cache.URL,
                RequestParameters.Cache.DOC,
                RequestParameters.Cache.OFFSET,
                RequestParameters.Cache.LENGTH);
        binder.registerCustomEditor(String.class, RequestParameters.PROFILE, new StringArrayFirstSlotEditor());
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
            @Valid CacheQuestion question) throws Exception {
        if(this.dLSEnabledChecker.isDLSEnabled(question.getCollection())) {
            log.trace("Cache copies not available as DLS is enabled for the collection");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        } else {
            return cacheNoSecurityCheck(request, response, question);
        }
        return new ModelAndView(CACHED_COPY_UNAVAILABLE_VIEW, new HashMap<String, Object>());
    }
    
    /**
     * Process cache request without doing security checks.
     * 
     * @param request
     * @param response
     * @param question
     * @return
     * @throws Exception
     */
    public ModelAndView cacheNoSecurityCheck(HttpServletRequest request,
            HttpServletResponse response,
            @Valid CacheQuestion question) throws Exception {
        if (question.getUrl() == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } else if (question.getCollection().getConfiguration().valueAsBoolean(Keys.UI_CACHE_DISABLED)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }  else { 
            RecordAndMetadata<? extends Record<?>> rmd
                    = dataRepository.getCachedDocument(question.getCollection(), StoreView.live, question.getUrl());
            if ((rmd == null || rmd.record == null) && question.getDoc() != null) {
                // Attempt with DOC parameter
                File docFile = RelativeFileOnlyEditor.transformToFile(question.getDoc());
                // Attempt with DOC parameter
                rmd = dataRepository.getDocument(question.getCollection(), StoreView.live, question.getUrl(), docFile, question.getOff(), question.getLen());
            }
                
            if (rmd != null && rmd.record != null) {
                rmd = new ConvertRecordType().convertRecord(rmd);
                HookScriptResult hookResult = runHookScript(question.getCollection(), Hook.pre_cache, rmd);
                if (hookResult.authorized) {
                    if (hookResult.record != null && hookResult.metadata != null) {
                        rmd = new RecordAndMetadata<Record<?>>(hookResult.record, hookResult.metadata);
                    }
                    
                    
                    if (rmd.record instanceof RawBytesRecord || rmd.record instanceof StringRecord) {
                        String charset = getCharset(rmd.metadata);
                        
                        String content = getContent(rmd.record, charset);
                        
                        Map<String, Object> model = new HashMap<String, Object>();
                        model.put(RequestParameters.Cache.URL, question.getUrl());
                        model.put(RequestParameters.COLLECTION, question.getCollection());
                        model.put(RequestParameters.PROFILE, question.getProfile());
                        model.put(RequestParameters.FORM, question.getForm());
                        model.put(MODEL_REQUEST_URL, new URL(request.getRequestURL().toString()));
                        
                        
                        Function<Multimap<String, String>, Map<String, String>> toSingleValue = (multiMap) -> {
                          Map<String, String> map = new HashMap<>();
                          for(Entry<String, String> e : multiMap.entries()) {
                              map.put(e.getKey(), e.getValue());
                          }
                          return map;
                        };
                        
                        //Provide a regular map for backwards compatibility
                        model.put(MODEL_METADATA_V1, toSingleValue.apply(rmd.metadata));
                        model.put(MODEL_METADATA_V2, rmd.metadata.asMap());
                        model.put(SearchController.ModelAttributes.httpRequest.toString(), request);
                        
                        
                        model.put(MODEL_DOCUMENT, Jsoup.parse(content, charset));
                        
                        String view = DefaultValues.FOLDER_CONF
                                + "/" + question.getCollection().getId()
                                + "/" + question.getProfile()
                                + "/" + question.getForm() + DefaultValues.CACHE_FORM_SUFFIX;

                        incrementMetrics(question.getCollection(), question.getProfile());
                        
                        return new ModelAndView(view, model);
                        
                    } else if (rmd.record instanceof XmlRecord) {
                        XmlRecord xmlRecord = (XmlRecord) rmd.record;
    
                        // Set custom content-type if any
                        response.setContentType(
                                question.getCollection().getConfiguration().value(
                                        Keys.ModernUI.Cache.FORM_PREFIX
                                        + "." + question.getForm() + "." + Keys.ModernUI.FORM_CONTENT_TYPE_SUFFIX,
                                DEFAULT_XML_CONTENT_TYPE));
                        
                        // XSL Transform if there's an XSL template
                        File xslTemplate = configRepository.getXslTemplate(question.getCollection().getId(), question.getProfile());
                        if (xslTemplate != null) {
                            Transformer transformer = transformerFactory.newTransformer(new StreamSource(xslTemplate));
                            DOMSource xmlSource = new DOMSource(xmlRecord.getContent());
                            transformer.transform(xmlSource, new StreamResult(response.getOutputStream()));
                        } else {
                            response.getWriter().write(XMLUtils.toString(xmlRecord.getContent()));
                        }

                        incrementMetrics(question.getCollection(), question.getProfile());
                        
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
                log.debug("Cached copy of '"+question.getUrl()+"' not found on collection '"+question.getCollection().getId()+"'");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        }
        
        return new ModelAndView(CACHED_COPY_UNAVAILABLE_VIEW, new HashMap<String, Object>());
    }
    
    /**
     * @param headers List of headers for the content, from the store
     * @return The charset for the document, defaulting to UTF-8 is not found
     */
    private static final Pattern charsetPattern = Pattern.compile("(?i)\\bcharset=\\s*\"?([^\\s;\"]*)");
    public String getCharset(Multimap<String, String> headers) {
        if (headers.containsKey(Store.Header.Charset.toString())) {
            return headers.get(Store.Header.Charset.toString()).stream().filter(v -> v != null).findFirst().get();
        }
        
        Optional<String> contentTypeValue = getContentTypeFromHeaders(headers);
        if(contentTypeValue.isPresent()) {
            Matcher m = charsetPattern.matcher(contentTypeValue.get());
            if (m.find()) {
              return m.group(1).trim();
            }
        }
        return DEFAULT_CHARSET;
    }
    
    /**
     * Gets the Content-Type http header value from the headers.
     * 
     * <p>This deals with the case the http headers are case insensitive</p>
     * @param headers
     * @return
     */
    private Optional<String> getContentTypeFromHeaders(Multimap<String, String> headers) {
        //Try standard case Content-Type
        Optional<String> value = headers.get(HttpHeaders.CONTENT_TYPE).stream().filter(v -> v != null).findFirst();
        if(value.isPresent()) return value;
        
        final String contentTypeLower = HttpHeaders.CONTENT_TYPE.toLowerCase();
        
        //Try lower case content-type
        value = headers.get(contentTypeLower).stream().filter(v -> v != null).findFirst();
        if(value.isPresent()) return value;
        
        //Look for headers where the case is messed up for example:
        //Content-type, CONTENT-TYPE, etc
        for(Map.Entry<String, String> h : headers.entries()) {
            if(contentTypeLower.equals(h.getKey().toLowerCase())) {
                return Optional.of(h.getValue());
            }
        }
        return Optional.empty();
    }
    
    /**
     * Get the content of a record as a String, using the default
     * charset.
     * @param r The record to get the content from
     * @return The content as a String
     */
    public String getContent(Record<?> r, String charset) {
        if (r instanceof RawBytesRecord) {
            try {
                return new String(((RawBytesRecord) r).getContent(), charset);
            } catch (UnsupportedEncodingException e) {
                log.debug("Unsuppported charset encountered using default charset for document: {}", r.getPrimaryKey());
                return new String(((RawBytesRecord) r).getContent());
            }
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
        public final ListMultimap<String, String> metadata;
    }
    
    /**
     * Handle bind exception (validation error, happens when a bad
     * parameter is passed in and cannot be mapped to an object field)
     * @param response
     */
    @ExceptionHandler(BindException.class)
    public void handleBindException(HttpServletResponse response, BindException be) {
        log.error("Validation error", be);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }
    
    /**
     * Display an error message is something went wrong
     * @param response HTTP Response, to set the status code to 500
     * @param e Exception that occurred
     * @return {@link ModelAndView}
     */
    @ExceptionHandler
    public ModelAndView handleException(HttpServletResponse response, Exception e) {
        log.error("An error occurred while processing a cache request", e);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.setContentType("text/html");
        return new ModelAndView(CACHED_COPY_UNAVAILABLE_VIEW, "exception", e);
    }
    
}
