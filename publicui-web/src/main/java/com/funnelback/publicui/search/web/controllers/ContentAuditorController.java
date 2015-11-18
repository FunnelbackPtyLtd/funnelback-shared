package com.funnelback.publicui.search.web.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.SearchQuestionType;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;

/**
 * Presents a content auditor interface.
 * 
 * Expects the same set of parameters as SerchController (which this uses internally), however some
 * search settings are overridden (e.g. the requested form is always ignored).
 */
@Controller
public class ContentAuditorController {

    /** Name of the default template used by content auditor */
    private static final String DEFAULT_TEMPLATE_NAME = "index";
    
    /** Name of the summary view for the Marketing Dashboard */
    private static final String SUMMARY_TEMPLATE_NAME = "summary";

    /** Name of the template for content auditor CSV export */
    private static final String CSV_TEMPLATE_NAME = "csv_export";

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
     * Content Auditor produces a content auditing report, using its
     * SearchController to get the necessary raw data.
     */
    @RequestMapping("/content-auditor.*")
    @PreAuthorize("hasRole('sec.content-auditor','ROLE_ANONYMOUS')")
    public ModelAndView generateContentAuditorReport(
            HttpServletRequest request,
            HttpServletResponse response,
            SearchQuestion question,
            @ModelAttribute SearchUser user) {
        // Mark this question as a content auditor one.
        // See the ContentAuditor input processor for what this triggers
        question.setQuestionType(SearchQuestionType.CONTENT_AUDITOR);

        // Pass off to the searchController
        ModelAndView mav = searchController.search(request, response, question, user);

        // Arrange to use the special content-auditor template for rendering
        String templateDirectory = question.getCollection().getConfiguration().value(Keys.ModernUI.ContentAuditor.TEMPLATE_DIRECTORY);
        String viewName = templateDirectory + "/" + ContentAuditorController.DEFAULT_TEMPLATE_NAME;
        if (ContentAuditorController.CSV_TEMPLATE_NAME.equals(question.getForm())) {
            viewName = templateDirectory + "/" + ContentAuditorController.CSV_TEMPLATE_NAME;
            response.setContentType("text/csv");
            response.setHeader("content-disposition", "attachment; filename=content-auditor-export.csv");            
        } else if (SUMMARY_TEMPLATE_NAME.equals(question.getForm())) {
            viewName = templateDirectory + "/" + SUMMARY_TEMPLATE_NAME;
            response.setContentType("application/json");
        }
        
        return new ModelAndView(viewName, mav.getModel());
    }

}
