package com.funnelback.publicui.search.web.controllers;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.PadreForkingException;

/**
 * Simple wrapper to run <code>padre-sw</code> directly.
 * 
 * @since v12.0
 */
@Controller
@Log4j2
public class PadreSwController extends AbstractRunPadreBinaryController {

    private static final String PADRE_SW = "padre-sw";
    
    @Autowired
    private File searchHome;

    @Override
    protected File getSearchHome() {
        return searchHome;
    }
    
    @RequestMapping("/padre-sw.cgi")
    public void padreSw(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        try {
            runPadreBinary(PADRE_SW, null, request, response, false, DefaultValues.ModernUI.PADRE_RESPONSE_SIZE_LIMIT);
        } catch (PadreForkingException e) {
            PadreSwController.log.debug("Unable to run " + PADRE_SW, e);
            throw new ServletException(e);
        }
    }


}
