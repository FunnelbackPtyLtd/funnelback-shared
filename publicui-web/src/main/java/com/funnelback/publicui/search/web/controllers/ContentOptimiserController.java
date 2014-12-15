package com.funnelback.publicui.search.web.controllers;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.AllArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.funnelback.publicui.search.web.views.json.MappingJacksonJsonpView;
import com.wordnik.swagger.annotations.ApiOperation;

@Controller
public class ContentOptimiserController {

    @Autowired
    private ConfigRepository configRepository;

    @Autowired
    private SearchController searchController;

    @InitBinder
    public void initBinder(DataBinder binder) {
        searchController.initBinder(binder);
    }

    @Autowired
    private FreeMarkerView contentOptimiserAnchorsPage;
    @RequestMapping("/content-optimiser.html/anchors.html")
    public ModelAndView visitContentOptimiserAnchorsPage(HttpServletRequest request) {

        Map<String, Object> model = new HashMap<String, Object>();

        List<String> paramNames = new ArrayList<String>();
        Enumeration<String> es = request.getParameterNames();
        while (es.hasMoreElements()) {
            paramNames.add(es.nextElement());
        }

        for(String paramName : paramNames ) {
            model.put(paramName, request.getParameter(paramName));
        }

        return new ModelAndView(contentOptimiserAnchorsPage, model);
    }
    
    @RequestMapping (value={
        "/content-optimiser/",
        "/content-optimiser.html/",
        "content-optimiser"})
    public String redirects(HttpServletRequest request) {

        String paramString = continueParametersFrom (
            request,
            RequestParameters.COLLECTION,
            RequestParameters.QUERY,
            RequestParameters.CONTENT_OPTIMISER_URL,
            RequestParameters.LOADED,
            RequestParameters.PROFILE);
        
        return "redirect:/content-optimiser.html" + paramString;
    }
    
    @AllArgsConstructor
    public class Foo {
        String a;
        String b;
    }
    
    @Autowired
    private MappingJacksonJsonpView jsonview;
    
    @RequestMapping("/content-api.json")
    @ApiOperation(
        value = "Gets the foo.",
        produces = "application/json"
     )
    public ModelAndView barbar() {
        Foo foo =  new Foo("hello", "hi");
        
        return new ModelAndView(jsonview, "nameeeee", foo);
    }
    

    @RequestMapping("/content-optimiser.html")
    public ModelAndView mainEntry(
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
            throw new RuntimeException("Missing profile parameter in Content Optimiser");
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
    public ModelAndView visitContentOptimiserResultsPage(HttpServletRequest request, HttpServletResponse response, SearchQuestion question, SearchUser user) {

        question.getRawInputParameters().put(RequestParameters.EXPLAIN, new String[] {"on"});
        question.getRawInputParameters().put(RequestParameters.NUM_RANKS, new String[] {"999"});

        ModelAndView mav =
            searchController.search(request, response, question, user);

        Map<String, Object> model = mav.getModel();

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

        paramValue = paramValue == null ? null : paramValue.trim();

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
