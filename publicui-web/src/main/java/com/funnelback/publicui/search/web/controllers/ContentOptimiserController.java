package com.funnelback.publicui.search.web.controllers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.log4j.Log4j2;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.freemarker.FreeMarkerView;

import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.web.controllers.SearchController.ModelAttributes;
import com.funnelback.publicui.search.web.controllers.SearchController.ViewTypes;
import com.funnelback.publicui.search.web.exception.ViewTypeNotFoundException;
@Log4j2
@Controller
public class ContentOptimiserController {

    
    private static final String REQUEST_MAPPING_PREFIX = "/seo-auditor";
    /**
     * Defines the mvc:mapping path= that would be required when setting up a intercepter
     * for the content optimiser.
     */
    public static final String REQUEST_MAPPING_MATCHER = REQUEST_MAPPING_PREFIX + ".*";
    
    /**
     * Authenticated users don't have the ROLE_ANONYMOUSE so they require sec.content-auditor 
     * anonymous users (which can only be over non admin) may be denied depending on what is in
     * global.cfg
     */
    private static final String PRE_AUTH = "hasAnyRole('sec.seo-auditor','ROLE_ANONYMOUS')";
    
    @Autowired
    private ConfigRepository configRepository;

    @Autowired
    private SearchController searchController;

    @InitBinder
    public void initBinder(DataBinder binder) {
        searchController.initBinder(binder);
    }
    
    @RequestMapping (value={
        "/content-optimiser/",
        "/content-optimiser.html/",
        "content-optimiser",
        "/seo-auditor/",
        "/seo-auditor.html/",
        "/seo-auditor"})
    @PreAuthorize(PRE_AUTH)
    public String redirects(HttpServletRequest request) {

        String paramString = continueParametersFrom (
            request,
            RequestParameters.COLLECTION,
            RequestParameters.QUERY,
            RequestParameters.CONTENT_OPTIMISER_URL,
            RequestParameters.LOADED,
            RequestParameters.PROFILE);
        
        return "redirect:/" + REQUEST_MAPPING_PREFIX + ".html" + paramString;
    }
    
    @RequestMapping(REQUEST_MAPPING_PREFIX + ".json")
    @PreAuthorize(PRE_AUTH)
    public ModelAndView mainEntryJson(
            HttpServletRequest request,
            HttpServletResponse response,
            SearchQuestion question,
            @ModelAttribute SearchUser user) {
        return mainEntryInner(request, response, question, user);
    }
    
    @RequestMapping(REQUEST_MAPPING_PREFIX + ".html")
    @PreAuthorize(PRE_AUTH)
    public ModelAndView mainEntry(
            HttpServletRequest request,
            HttpServletResponse response,
            SearchQuestion question,
            @ModelAttribute SearchUser user) {
        return mainEntryInner(request, response, question, user);
    }
    
    private ModelAndView mainEntryInner(
        HttpServletRequest request,
        HttpServletResponse response,
        SearchQuestion question,
        @ModelAttribute SearchUser user) {
        String collection = request.getParameter(RequestParameters.COLLECTION);
        String query = request.getParameter(RequestParameters.QUERY);
        String optimiserUrl = request.getParameter(RequestParameters.CONTENT_OPTIMISER_URL);
        String loaded = request.getParameter(RequestParameters.LOADED);

        String profile = request.getParameter(RequestParameters.PROFILE);
        if(profile == null) {
            throw new RuntimeException("Missing profile parameter in SEO Auditor");
        }

        //Check for each parameter, so we can route the user to the right page
        if(nullOrEmpty(collection)) {

            //Go back and choose the collection
            return visitContentOptimiserChooseCollectionPage();

        //} else if (nullOrEmpty(query, optimiserUrl)) {
        } else if (nullOrEmpty(query)) {
            //Go back and choose the query and optimiserUrl
            return visitContentOptimiserCollectionQueryPage(request);

        } else if (nullOrEmpty(loaded)) {

            //Plumb the parameters through the loading page so we can get them back later
            Map<String, String> loadingPageModel = new TreeMap<String, String>();
            loadingPageModel.put(RequestParameters.COLLECTION, collection);
            loadingPageModel.put(RequestParameters.PROFILE, profile);
            loadingPageModel.put(RequestParameters.QUERY, query);

            if(nullOrEmpty(optimiserUrl)) {
                loadingPageModel.put(RequestParameters.CONTENT_OPTIMISER_URL, "");
            } else {
                loadingPageModel.put(RequestParameters.CONTENT_OPTIMISER_URL, optimiserUrl.trim());
            }

            //Head to loading page
            return visitContentOptimiserLoadingPage(loadingPageModel);

        } else {

            //Head to results page
            return visitContentOptimiserResultsPage(request, response, question, user);
        }
    }

    @Autowired
    private FreeMarkerView contentOptimiserChooseCollectionPage;
    public ModelAndView visitContentOptimiserChooseCollectionPage() {

        //Pass the collection names through for FreeMarker to render
        Map<String, Object> model = new HashMap<String, Object>();
        model.put(
            ModelAttributes.AllCollections.toString(),
            configRepository.getAllCollections());

        return new ModelAndView(contentOptimiserChooseCollectionPage, model);
    }

    @Autowired
    private FreeMarkerView contentOptimiserCollectionQueryPage;
    public ModelAndView visitContentOptimiserCollectionQueryPage(HttpServletRequest request) {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put(RequestParameters.COLLECTION, request.getParameter(RequestParameters.COLLECTION));            
        return new ModelAndView(contentOptimiserCollectionQueryPage, model);
    }

    @Autowired
    private FreeMarkerView contentOptimiserLoadingPage;
    public ModelAndView visitContentOptimiserLoadingPage(Map<String, String> loadingPageModel) {
        return new ModelAndView(contentOptimiserLoadingPage, loadingPageModel);
    }

    @Autowired
    private FreeMarkerView contentOptimiserResultsPage;
    
    public ModelAndView visitContentOptimiserResultsPage(HttpServletRequest request, 
                                                            HttpServletResponse response, 
                                                            SearchQuestion question, 
                                                            SearchUser user) {

        question.getRawInputParameters().put(RequestParameters.EXPLAIN, new String[] {"on"});
        question.getRawInputParameters().put(RequestParameters.NUM_RANKS, new String[] {"999"});

        ModelAndView mav =
            searchController.search(request, response, question, user);

        Map<String, Object> model = mav.getModel();
        
        try {
            ViewTypes vt = ViewTypes.valueOf(FilenameUtils.getExtension(request.getRequestURI()));
            if(vt.equals(ViewTypes.json)) {
                return new ModelAndView("json", model);
            }
        } catch (IllegalArgumentException iae) {
            log.warn("SEO auditor called with an unknown extension '"+request.getRequestURL()+"'.");
            throw new ViewTypeNotFoundException(FilenameUtils.getExtension(request.getRequestURI()));
        }
        

        return new ModelAndView(contentOptimiserResultsPage, model);
    }

    // Turning off modeldump functionality - only turn it on for debugging
    @RequestMapping("content-optimiser.html/modeldump.html/")
    public String redirectDump() {
        return "redirect:/content-optimiser.html";
    }

    @Autowired
    private FreeMarkerView contentOptimiserModelDump;
    @RequestMapping("content-optimiser.html/modeldump.html")
    public ModelAndView visitContentOptimiserModelDump(
            HttpServletRequest request,
            HttpServletResponse response,
            SearchQuestion question,
            SearchUser user) {

        question
            .getRawInputParameters()
            .put(RequestParameters.EXPLAIN, new String[] {"on"});

        question
            .getRawInputParameters()
            .put(RequestParameters.NUM_RANKS, new String[] {"999"});

        Map<String, Object> m = searchController.search(request, response, question, user).getModel();

        return new ModelAndView(contentOptimiserModelDump, m);
    }

    /** Pulls out the selected parameters by name, 
     * and re-assembles them */
    private static String continueParametersFrom(HttpServletRequest request, String...paramNames) {
        StringBuffer sb = new StringBuffer();
        sb.append("?");
        for(String paramName : paramNames) {
            String next = continueParameterFrom(request, paramName);
            if (next.length() > 0) {
                sb.append(next);
                sb.append("&");
            }
        }
        return sb.substring(0, sb.length()-1);
    }
    
    /** Return the given parameter=value pair as a string, or "" */
    private static String continueParameterFrom(HttpServletRequest request, String paramName) {
        String paramValue = request.getParameter(paramName);

        try {
        	paramValue = paramValue == null ? null : URLEncoder.encode(paramValue.trim(), "UTF-8");
        } catch (UnsupportedEncodingException uee) {
        	paramValue = null;
        }

        return nullOrEmpty(paramValue)
            ? ""
            : paramName + "=" + paramValue;
    }

    /** Checks if any of the given strings are empty or null */
    private static boolean nullOrEmpty(String...params) {
        for(String s : params) {
            if(s == null || s.equals("")) {
                return true;
            }
        }
        return false;
    }

}
