package com.funnelback.publicui.accessibilityauditor.web.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.SearchQuestionType;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.web.controllers.SearchController;
import com.funnelback.publicui.search.web.controllers.StreamResultsController;
import com.funnelback.publicui.streamedresults.CommaSeparatedList;

/**
 * Accessibility Auditor controller. Delegates the actual
 * search to the {@link SearchController}
 * 
 * @author nguillaumin@funnelback.com
 *
 */
@Controller
public class AccessibilityAuditorController {

    /**
     * Users need the sec.accessibility-auditor role to access the reports. When developing
     * with the additional port set, there is usually no authentication when
     * running the app. from Eclipse, so ROLE_ANONYMOUS needs to be permitted.
     * 
     * In practice users cannot access AA on the non admin port (due to an interceptor)
     * so they will always use the admin port which requires authentication.
     */
    private static final String PRE_AUTH = "hasAnyRole('sec.accessibility-auditor','ROLE_ANONYMOUS')"; 
    
    @Autowired
    private SearchController searchController;
    
    @Autowired
    private StreamResultsController streamResultsController;

    @InitBinder
    public void initBinder(DataBinder binder) {
        searchController.initBinder(binder);
    }

    @RequestMapping("/accessibility-auditor.json")
    @PreAuthorize(PRE_AUTH)
    public ModelAndView audit(
            HttpServletRequest request,
            HttpServletResponse response,
            SearchQuestion question) {
        return runQuery(request, response, question, SearchQuestionType.ACCESSIBILITY_AUDITOR);
    }
    
    
    @RequestMapping("/accessibility-auditor-acknowledgement-counts.json")
    @PreAuthorize(PRE_AUTH)
    public ModelAndView acknowledgementCounts(
            HttpServletRequest request,
            HttpServletResponse response,
            SearchQuestion question) {
        return runQuery(request, response, question, SearchQuestionType.ACCESSIBILITY_AUDITOR_ACKNOWLEDGEMENT_COUNTS);
    }
    
    
    @RequestMapping("/accessibility-auditor-all-results.*")
    @PreAuthorize(PRE_AUTH)
    public void getAllResults(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestParam(required=false) CommaSeparatedList fields,
        @RequestParam(required=false) CommaSeparatedList fieldnames,
        @RequestParam(required=false, defaultValue="true") boolean optimisations,
        @Valid SearchQuestion question,
        @ModelAttribute SearchUser user) throws Exception {
        
        streamResultsController.getAllResults(request, response, fields, fieldnames, optimisations, question, user,
            SearchQuestionType.ACCESSIBILITY_AUDITOR_GET_ALL_RESULTS);
    }
    
    public ModelAndView runQuery(
        HttpServletRequest request,
        HttpServletResponse response,
        SearchQuestion question, 
        SearchQuestionType searchQuestionType) {

        question.setQuestionType(searchQuestionType);

        ModelAndView mav = searchController.search(request, response, question, null);

        if (mav != null) {
            return new ModelAndView((String) null, mav.getModel());
        } else {
            return null;
        }
}
}
