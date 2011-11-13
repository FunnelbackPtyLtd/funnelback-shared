package com.funnelback.publicui.search.web.controllers;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.apachecommons.CommonsLog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.PadreForkingException;
import com.funnelback.publicui.search.model.transaction.Suggestion;
import com.funnelback.publicui.search.service.Suggester;
import com.funnelback.publicui.search.service.Suggester.AutoCMode;
import com.funnelback.publicui.search.service.Suggester.Sort;

/**
 * Query completion / suggestion controller. Wrapper around 'padre-qs'
 */
@Controller
@CommonsLog
public class SuggestController extends AbstractRunPadreBinaryController {

	private static final String PADRE_QS = "padre-qs";

	@Autowired
	private File searchHome;
	
	@Autowired
	private Suggester suggester;

	@RequestMapping("/suggest.json")
	public void suggest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		try {
			runPadreBinary(PADRE_QS, null, request, response);
		} catch (PadreForkingException e) {
			log.error("Unable to run " + PADRE_QS, e);
			throw new ServletException(e);
		}
	}
	
	@RequestMapping(value="/suggest-java.json",params={"collection","partial_query","show","sort","alpha","autoc","fmt"})
	public String suggestJava(String collection,
			String partial_query,
			int show,
			int sort,
			float alpha,
			int autoc,
			String fmt,
			HttpServletResponse response) throws IOException {
		// collection=funnelback_documentation&partial_query=que&show=10&sort=0&alpha=.5&autoc=0&fmt=json
		List<Suggestion> suggestions = suggester.suggest(collection, partial_query, show, Sort.valueOf(sort), alpha, AutoCMode.valueOf(autoc));
		
		StringBuilder sb = new StringBuilder("[");
		for (int i=0; i<suggestions.size() && i<10; i++) {
			sb.append("\"").append(suggestions.get(i).suggestion).append("\"");
			if (i+1 < suggestions.size() && i+1<10) {
				sb.append(",");
			}
		}
		sb.append("]");
		
		response.getWriter().write(sb.toString());
		
		return null;
		
		
	}

	@Override
	protected File getSearchHome() {
		return searchHome;
	}

}
