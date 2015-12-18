package com.funnelback.publicui.search.web.controllers;

import java.util.Map;

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

import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;

import lombok.extern.log4j.Log4j2;
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
     * Authenticated users don't have the ROLE_ANONYMOUS so they require sec.content-auditor 
     * anonymous users (which can only be over non admin) may be denied depending on what is in
     * global.cfg
     */
    private static final String PRE_AUTH = "hasAnyRole('sec.seo-auditor','ROLE_ANONYMOUS')";
    
    @Autowired
    private SearchController searchController;

    @InitBinder
    public void initBinder(DataBinder binder) {
        searchController.initBinder(binder);
    }
    

    @RequestMapping(value=REQUEST_MAPPING_PREFIX + ".json", params={
                    RequestParameters.COLLECTION,
                    RequestParameters.PROFILE,
                    RequestParameters.QUERY,
                    RequestParameters.CONTENT_OPTIMISER_URL
    })
    @PreAuthorize(PRE_AUTH)
    public ModelAndView mainEntryJson(
            HttpServletRequest request,
            HttpServletResponse response,
            SearchQuestion question,
            @ModelAttribute SearchUser user) {
        
        question.getRawInputParameters().put(RequestParameters.EXPLAIN, new String[] {"on"});
        question.getRawInputParameters().put(RequestParameters.NUM_RANKS, new String[] {"999"});

        ModelAndView mav =
            searchController.search(request, response, question, user);

        Map<String, Object> model = mav.getModel();
        
        return new ModelAndView("json", model);
    }

}
