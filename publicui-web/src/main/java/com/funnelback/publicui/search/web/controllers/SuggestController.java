package com.funnelback.publicui.search.web.controllers;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.apachecommons.Log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.PadreForkingException;

/**
 * Query completion / suggestion controller. Wrapper around 'padre-qs'
 */
@Controller
@Log
public class SuggestController extends AbstractRunPadreBinaryController {

	private static final String PADRE_QS = "padre-qs";


	@Autowired
	private File searchHome;

	@RequestMapping("/suggest.json")
	public void suggest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		try {
			runPadreBinary(PADRE_QS, null, request, response);
		} catch (PadreForkingException e) {
			log.error("Unable to run " + PADRE_QS, e);
			throw new ServletException(e);
		}
	}

	@Override
	protected File getSearchHome() {
		return searchHome;
	}

}
