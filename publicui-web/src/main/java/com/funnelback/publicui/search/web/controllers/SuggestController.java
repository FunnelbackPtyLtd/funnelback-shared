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
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.PadreForkingException;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.Suggester;
import com.funnelback.publicui.search.service.Suggester.Sort;
import com.funnelback.publicui.search.web.binding.CollectionEditor;

/**
 * Query completion / suggestion controller.
 */
@Controller
@Log4j
public class SuggestController extends AbstractRunPadreBinaryController {

	private static final String PADRE_QS = "padre-qs";
	
	@Autowired
	private ConfigRepository configRepository;
	
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

	/**
	 * Simple Wrapper around <code>padre-qs.cgi</code>
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws ServletException
	 * @deprecated Use {@link #suggestJava(String, String, String, int, int, String, String)} instead
	 */
	@Deprecated
	@RequestMapping(value="/padre-qs.cgi", params=RequestParameters.COLLECTION)
	public void suggest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		try {
			runPadreBinary(PADRE_QS, null, request, response, true);
		} catch (PadreForkingException e) {
			SuggestController.log.error("Unable to run " + PADRE_QS, e);
			throw new ServletException(e);
		}
	}

	@InitBinder
	public void initBinder(DataBinder binder) {
		binder.registerCustomEditor(Collection.class, new CollectionEditor(configRepository));
	}

	@RequestMapping(value="/suggest.json",params="!"+RequestParameters.COLLECTION)
	public void noCollection(HttpServletResponse response) {
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	}
	
	/**
	 * Use the default suggester service, usually backed by LibQS.
	 * 
	 * @param collection
	 * @param profile
	 * @param partialQuery First letters of a query
	 * @param show Number of items to show
	 * @param sort Order for suggestions (See LibQS code for possible values)
	 * @param format JSON or XML
	 * @param callback Name of a JSONP callback if needed
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value="/suggest.json",params=RequestParameters.COLLECTION)
	public ModelAndView suggestJava(@RequestParam("collection") String collectionId,
			@RequestParam(defaultValue=DefaultValues.DEFAULT_PROFILE) String profile,
			@RequestParam("partial_query") String partialQuery,
			@RequestParam(defaultValue="10") int show,
			@RequestParam(defaultValue="0") int sort,
			@RequestParam(value="fmt",defaultValue="json") String format,
			String callback,
			HttpServletResponse response) throws IOException {
		
		Collection c = configRepository.getCollection(collectionId);
		if (c != null) {
			ModelAndView mav = new ModelAndView();
			mav.addObject("suggestions", suggester.suggest(c, profile, partialQuery, show, Sort.valueOf(sort)));
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
		} else {
			// Collection not found
			log.warn("Collection '"+collectionId+"' not found");
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}
	}

	@Override
	protected File getSearchHome() {
		return searchHome;
	}

}
