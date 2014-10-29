package com.funnelback.publicui.search.web.controllers;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.log4j.Log4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;

/**
 * Presents a content auditor interface.
 * 
 * Expects the same set of parameters as SerchController (which this uses internally), however some
 * search settings are overridden (e.g. the requested form is always ignored).
 */
@Controller
@Log4j
public class ContentAuditorController {

    /**
     * SearchController is used to perform the actual search requests to create the auditor report
     */
    @Autowired
    private SearchController searchController;

    /**
     * We apply the SearchController's initBinder (since we want to replicate how it handles requests).
     */
    @InitBinder
    public void initBinder(DataBinder binder) {
        searchController.initBinder(binder);
    }
    
    /**
     * Main entry point for Content Auditor
     * 
     * This method produces a content auditing report, using its
     * SearchController to get the necessary raw data.
     */
    @RequestMapping("/content-auditor.*")
    public ModelAndView generateContentAuditorReport(
            HttpServletRequest request,
            HttpServletResponse response,
            SearchQuestion question,
            @ModelAttribute SearchUser user,
            String type) {

        System.out.println(request.getRequestURL() + "?" + request.getQueryString());
        
        question.getCollection().getConfiguration().setValue("ui.modern.search_link", "content-auditor.html");
        
        // TODO - Manipulate the request if we need to
        question.getRawInputParameters().put(RequestParameters.NUM_RANKS, new String[] {"999"});

        if (question.getQuery() == null) {
            question.setQuery("-padrenullquery");
        }
        
        // TODO - Manipulate the facet config (somehow)
        
        ModelAndView mav =
            searchController.search(request, response, question, user);
        
        Map<String, Object> model = mav.getModel();

        // Content auditor always uses the specific content auditor template
        
        if (type == null) {
            type = "index";
        }
        
        String viewName = 
            DefaultValues.FOLDER_WEB + "/" +
            DefaultValues.FOLDER_TEMPLATES + "/" +
            DefaultValues.FOLDER_MODERNUI + "/" +
            DefaultValues.FOLDER_CONTENT_AUDITOR + "/" +
            type;
        
        return new ModelAndView(viewName, model);
    }

}
