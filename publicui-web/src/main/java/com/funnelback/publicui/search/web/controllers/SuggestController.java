package com.funnelback.publicui.search.web.controllers;

import java.io.File;
import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.log4j.Log4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.PadreForkingException;
import com.funnelback.publicui.search.service.Suggester;
import com.funnelback.publicui.search.service.Suggester.Sort;

/**
 * Query completion / suggestion controller. Wrapper around 'padre-qs'
 */
@Controller
@Log4j
public class SuggestController extends AbstractRunPadreBinaryController {

	private static final String PADRE_QS = "padre-qs";
	
	
	private enum Format {
		Json("json"), JsonPlus("json++"), JsonPlusBad("json  ");
		
		public final String value;
		
		private Format(String value) {
			this.value = value;
		}
		
		public static Format fromValue(String value) {
			for (Format f: Format.values()) {
				if (f.value.equals(value)) {
					return f;
				}
			}
			throw new IllegalArgumentException(value);
		}
	}

	@Autowired
	private File searchHome;
	
	@Autowired
	private Suggester suggester;
	
	@Resource(name="suggestViewSimple")
	private View simpleView;
	
	@Resource(name="suggestViewRich")
	private View richView;

	@RequestMapping("/padre-qs.cgi")
	public void suggest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		try {
			runPadreBinary(PADRE_QS, null, request, response, false);
		} catch (PadreForkingException e) {
			SuggestController.log.error("Unable to run " + PADRE_QS, e);
			throw new ServletException(e);
		}
	}
	
	@RequestMapping(value="/suggest.json")
	public ModelAndView suggestJava(String collection,
			@RequestParam(defaultValue=DefaultValues.DEFAULT_PROFILE) String profile,
			@RequestParam("partial_query") String partialQuery,
			@RequestParam(defaultValue="10") int show,
			@RequestParam(defaultValue="0") int sort,
			@RequestParam(value="fmt",defaultValue="json") String format,
			String callback) throws IOException {
		
		ModelAndView mav = new ModelAndView();
		mav.addObject("suggestions", suggester.suggest(collection, profile, partialQuery, show, Sort.valueOf(sort)));
		mav.addObject("callback", callback);
		
		switch(Format.fromValue(format)) {
		case Json:
			mav.setView(simpleView);
			break;
		case JsonPlus:
		case JsonPlusBad:
			mav.setView(richView);
			break;
		default:
			throw new IllegalArgumentException("Unrecognized format " + format);
		}
		
		return mav;
	}

	@Override
	protected File getSearchHome() {
		return searchHome;
	}

}
