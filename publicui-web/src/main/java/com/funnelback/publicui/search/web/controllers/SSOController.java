package com.funnelback.publicui.search.web.controllers;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.saml.metadata.MetadataManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/saml")
public class SSOController {

    // Logger
    private static final Logger LOG = LoggerFactory
            .getLogger(SSOController.class);

    @Autowired
    private MetadataManager metadata;

    @RequestMapping(value = "/idpSelection", method = RequestMethod.GET)
    public ModelAndView idpSelection(HttpServletRequest request, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
            LOG.warn("The current user is already logged.");
            return new ModelAndView("redirect:/search.html");
        } else {
            if (isForwarded(request)) {
                Set<String> idps = metadata.getIDPEntityNames();
                for (String idp : idps)
                    LOG.info("Configured Identity Provider for SSO: " + idp);
                model.addAttribute("idps", idps);
                model.addAttribute("entityID", request.getParameter("entityID"));
                return new ModelAndView("web/templates/modernui/saml/idpselection", model.asMap());
            } else {
                LOG.warn("Direct accesses to '/idpSelection' route are not allowed");
                return new ModelAndView("redirect:/");
            }
        }
    }

    /*
     * Checks if an HTTP request is forwarded from servlet.
     */
    private boolean isForwarded(HttpServletRequest request){
        if (request.getAttribute("javax.servlet.forward.request_uri") == null)
            return false;
        else
            return true;
}
    
}
