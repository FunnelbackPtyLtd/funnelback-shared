package com.funnelback.publicui.search.web.controllers;

import static com.funnelback.publicui.utils.web.MetricsConfiguration.ALL_NS;
import static com.funnelback.publicui.utils.web.MetricsConfiguration.VIEW_TYPE_NS;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import lombok.extern.log4j.Log4j2;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;

import com.codahale.metrics.MetricRegistry;
import com.funnelback.common.Environment.FunnelbackVersion;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.publicui.search.lifecycle.SearchTransactionProcessor;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.web.binding.CollectionEditor;
import com.funnelback.publicui.search.web.binding.SearchQuestionBinder;
import com.funnelback.publicui.search.web.binding.StringArrayFirstSlotEditor;
import com.funnelback.publicui.search.web.controllers.session.SessionController;
import com.funnelback.publicui.search.web.exception.ViewTypeNotFoundException;
import com.funnelback.publicui.utils.web.ExecutionContextHolder;

import freemarker.template.TemplateException;

/**
 * <p>Main controller for the Modern UI.</p>
 * 
 * <ul>
 *     <li>Deal with special cases like the collection list page</li>
 *  <li>Processes input parameters</li>
 *  <li>Call the transaction processor to process the search</li>
 *  <li>Select the correct view (HTML, JSON, XML ...)</li>
 * </ul>
 *
 */
@Controller
@Log4j2
public class SearchController extends SessionController {

    /**
     * Attribute keys used in the Spring model
     */
    public enum ModelAttributes {
        SearchTransaction, AllCollections, QueryString, SearchPrefix, ContextPath, Log,
        extraSearches, question, response, session, error, httpRequest, GlobalResourcesPrefix;
        
        public static Set<String> getNames() {
            HashSet<String> out = new HashSet<String>();
            for (ModelAttributes name: values()) {
                out.add(name.toString());
            }
            return out;
        }
    }
    
    /** The relative location of global resources used by the default search form and some other
     * forms in the modern UI. I think this can be accessed in the model with 
     * {@link ModelAttributes#GlobalResourcesPrefix}
     */
    public static final String GLOBAL_RESOURCES_LOCATION = "resources-global/";
    
    /**
     * The mapping used in our servlet config to map the global resources location
     */
    public static final String GLOBAL_RESOURCES_MAPPING = "/" + GLOBAL_RESOURCES_LOCATION + "/**";

    /**
     * Supported view types to return results. Tied to the
     * extension used in the HTTP request (e.g. <tt>search.xml</tt>)
     */
    public enum ViewTypes {
        html, htm, xml, json, classic;
    }

    @Autowired
    private SearchTransactionProcessor processor;
    
    @Autowired
    private ConfigRepository configRepository;
    
    @Autowired
    private ExecutionContextHolder executionContextHolder;
    
    @Autowired
    private FunnelbackVersion funnelbackVersion;
    
    @Autowired
    private LocaleResolver localeResolver;
    
    @Autowired
    private MetricRegistry metrics;

    /**
     * <p>Configures the binder to:</p>
     * <ul>
     *     <li>Restrict which URL parameters can be mapped to Java objects</li>
     *     <li>Convert a collection ID into a proper collection object</li>
     * </ul>
     * @param binder
     * @see "http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/html/mvc.html#mvc-ann-webdatabinder"
     */
    @InitBinder
    public void initBinder(DataBinder binder) {
        // For security reasons, only allow specific fields for
        // data binding
        binder.setAllowedFields(
                RequestParameters.CLIVE,
                RequestParameters.COLLECTION,
                RequestParameters.FORM,
                RequestParameters.PROFILE,
                RequestParameters.QUERY    );
        binder.registerCustomEditor(Collection.class, new CollectionEditor(configRepository));
        binder.registerCustomEditor(String.class, RequestParameters.PROFILE, new StringArrayFirstSlotEditor());
    }
    
    @RequestMapping(value="/")
    public String index() {
        return "redirect:/search.html";
    }
    
    /**
     * Called when no collection has been specified.
     * @return a list of all available collections.
     */
    @RequestMapping(value={"/search.html"},params="!"+RequestParameters.COLLECTION)
    public ModelAndView noCollection(HttpServletResponse response) {
        return noCollection(response, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Helper method to return the collection list with a given status
     * @param response 
     * @param status Status code to return
     * @return
     */
    private ModelAndView noCollection(HttpServletResponse response, HttpStatus status) {
        if (status != null) {
            response.setStatus(status.value());
        }
        Map<String, Object> model = new HashMap<String, Object>();
        model.put(ModelAttributes.AllCollections.toString(), configRepository.getAllCollections());

        return new ModelAndView(DefaultValues.FOLDER_WEB+"/"
                +DefaultValues.FOLDER_TEMPLATES+"/"
                +DefaultValues.FOLDER_MODERNUI+"/no-collection", model);
    }

            
    /**
     * Default handler when we have a query and a collection.
     * @param request HTTP request
     * @param response HTTP response
     * @param question Search input parameters
     * @param user User to record search history for. Can be null if no
     *  search history should be recorded
     * @return
     */
    @RequestMapping(value="/search.*",params={RequestParameters.COLLECTION})
    public ModelAndView search(
            HttpServletRequest request,
            HttpServletResponse response,
            @Valid SearchQuestion question,
            @ModelAttribute SearchUser user) {

        // Put the relevant objects in the model, depending
        // of the view requested
        ViewTypes vt;
        try {
            vt = ViewTypes.valueOf(FilenameUtils.getExtension(request.getRequestURI()));
        } catch (IllegalArgumentException iae) {
            log.warn("Search called with an unknown extension '"+request.getRequestURL()+"'.");
            throw new ViewTypeNotFoundException(FilenameUtils.getExtension(request.getRequestURI()));
        }
        
        SearchTransaction transaction = null;
        
        if (question.getCollection() != null) {
            // This is were the magic happens. The TransactionProcessor
            // will take care of processing the search request.
            SearchQuestionBinder.bind(executionContextHolder.getExecutionContext(), request, question, localeResolver, funnelbackVersion);
            transaction = processor.process(question, user);
        } else {
            // Collection is null = non existent
            if (request.getParameter(SearchQuestion.RequestParameters.COLLECTION) != null) {
                log.warn("Collection '" + request.getParameter(SearchQuestion.RequestParameters.COLLECTION) + "' not found");
            }
            if (ViewTypes.htm.equals(vt) || ViewTypes.html.equals(vt)) {
                return noCollection(response, HttpStatus.NOT_FOUND);
            } else {
                response.setStatus(HttpStatus.NOT_FOUND.value());
                return null;
            }
        }
        
        if (transaction.getError() != null) {
            // Error occurred while processing the transaction, set the
            // response status code accordingly
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        
        metrics.counter(MetricRegistry.name(ALL_NS, VIEW_TYPE_NS, vt.name())).inc();
        
        Map<String, Object> model = getModel(vt, request, transaction);

        // Generate the view name, relative to the Funnelback home
        String viewName = DefaultValues.FOLDER_CONF + "/"
            + question.getCollection().getId()    + "/"
            + question.getProfile() + "/"
            + question.getForm();
        log.debug("Selected view '" + viewName + "'");
        
        return new ModelAndView(viewName, model);

    }
    
    private Map<String, Object> getModel(ViewTypes vt, HttpServletRequest request, SearchTransaction st) {
        Map<String, Object> out = new HashMap<String, Object>();
        switch (vt) {
        case json:
        case html:
        case htm:
            // WARNING: Make sure keys used here match the name of
            // the field on the SearchTransaction class
            out.put(ModelAttributes.question.toString(), st.getQuestion());
            out.put(ModelAttributes.response.toString(), st.getResponse());
            out.put(ModelAttributes.session.toString(), st.getSession());
            out.put(ModelAttributes.error.toString(), st.getError());
            if (st.getExtraSearches().size() > 0) {
                out.put(ModelAttributes.extraSearches.toString(), st.getExtraSearches());
            }
            out.put(ModelAttributes.QueryString.toString(), request.getQueryString());
            
            if (!ViewTypes.json.equals(vt)) {
                out.put(ModelAttributes.httpRequest.toString(), request);
            }
            break;
        case xml:
        case classic:
        default:
            out.put(ModelAttributes.SearchTransaction.toString(), st);
        }
        
        return out;
    }
    
    @ExceptionHandler(ViewTypeNotFoundException.class)
    public void viewTypeNotFound(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }
    
    @ExceptionHandler(TemplateException.class)
    public void templateException(TemplateException ex) {
        log.error("Error processing FreeMarker template", ex);
    }
    
    @ExceptionHandler(Exception.class)
    public void exception(Exception ex) {
        log.catching(ex);
    }
    
}
