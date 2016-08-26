package com.funnelback.publicui.accessibilityauditor.web.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.SearchQuestionType;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.web.controllers.SearchController;

/**
 * Accessibility Auditor controller. Delegates the actual
 * search to the {@link SearchController}
 * 
 * @author nguillaumin@funnelback.com
 *
 */
@Controller
public class AccessibilityAuditorController {

    @Autowired
    private SearchController searchController;

    @InitBinder
    public void initBinder(DataBinder binder) {
        searchController.initBinder(binder);
    }

    @RequestMapping("/accessibility-auditor.json")
    @PreAuthorize("T(java.net.InetAddress).getByName(#request.getRemoteAddr()).isLoopbackAddress()")
    // FIXME: @PreAuthorize("hasRole('sec.wcag')")
    public ModelAndView audit(
            HttpServletRequest request,
            HttpServletResponse response,
            SearchQuestion question) {
        
        question.setQuestionType(SearchQuestionType.ACCESSIBILITY_AUDITOR);
        
        ModelAndView mav = searchController.search(request, response, question, null);
        
        if (mav != null) {
            return new ModelAndView((String) null, mav.getModel());
        } else {
            return null;
        }
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public void accessDenied(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    }
}
