package com.funnelback.publicui.search.web.controllers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.funnelback.springmvc.utils.saml.SamlAuthUtil;

/**
 * <p>Controller for checking SAML auth status and redirecting to the saml IdP if required.</p>
 */
@Controller
public class SamlAuthController {

    /**
     * Login page for authenticating with SAML. This endpoint returns an empty document which
     * redirects to the provided redirectTo url. Note: the redirectTo url should be the pathname -
     * starting with /, absolute paths e.g. // or http:// are not allowed.
     * 
     * @param redirectTo - url to redirect to upon successful SAML authentication.
     */
    @RequestMapping(value = "/saml.html")
    public void login(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestParam(required = false) String redirectTo) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        SamlAuthUtil.writeSamlLoginDocumentWithRedirect(response.getWriter(), redirectTo);
    }

}
