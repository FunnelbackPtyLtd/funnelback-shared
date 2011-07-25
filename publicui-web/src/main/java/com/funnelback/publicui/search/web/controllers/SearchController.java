package com.funnelback.publicui.search.web.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.publicui.search.lifecycle.SearchTransactionProcessor;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.web.binding.CollectionEditor;
import com.funnelback.publicui.search.web.binding.SearchQuestionBinder;
import com.funnelback.publicui.search.web.exception.ViewTypeNotFoundException;

@Controller
@lombok.extern.apachecommons.Log
public class SearchController {

	public enum ModelAttributes {
		SearchTransaction, AllCollections, QueryString, SearchPrefix, ContextPath,
		extra, question, response, error;
		
		public static Set<String> getNames() {
			HashSet<String> out = new HashSet<String>();
			for (ModelAttributes name: values()) {
				out.add(name.toString());
			}
			return out;
		}
	}
	
	public enum ViewTypes {
		html, htm, xml, json, legacy;
	}

	@Autowired
	private SearchTransactionProcessor processor;
	
	@Autowired
	private ConfigRepository configRepository;

	@InitBinder
	public void initBinder(DataBinder binder) {
		binder.registerCustomEditor(Collection.class, new CollectionEditor(configRepository));
	}
	
	@RequestMapping(value={"/"})
	public String index() {
		return "redirect:/search.html";
	}
	
	/**
	 * Called when no collection has been specified.
	 * @return a list of all available collections.
	 */
	@RequestMapping(value={"/search.*"},params="!"+RequestParameters.COLLECTION)
	public ModelAndView noCollection() {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(ModelAttributes.AllCollections.toString(), configRepository.getAllCollections());

		// FIXME: Hack for the XML view that serialise only one item from the model
		model.put(ModelAttributes.SearchTransaction.toString(), configRepository.getAllCollections());

		return new ModelAndView(DefaultValues.FOLDER_WEB+"/"
				+DefaultValues.FOLDER_TEMPLATES+"/"
				+DefaultValues.FOLDER_PUBLICUI+"/no-collection", model);
	}

			
	/**
	 * Default handler when we have a query and a collection.
	 * @param request
	 * @param question
	 * @return
	 */
	@RequestMapping(value="/search.*",params={RequestParameters.COLLECTION})
	public ModelAndView search(
			HttpServletRequest request,
			@Valid SearchQuestion question) {
				
		SearchTransaction transaction = null;
		SearchQuestionBinder.bind(request, question);
		
		if (question.getCollection() != null) {
			if (question.getQuery() != null && ! "".equals(question.getQuery())) {
				transaction = processor.process(question);
			} else {
				// Query is null
				transaction = new SearchTransaction(question, null);
			}
		} else {
			// Collection is null = non existent
			if (request.getParameter(SearchQuestion.RequestParameters.COLLECTION) != null) {
				log.warn("Collection '" + request.getParameter(SearchQuestion.RequestParameters.COLLECTION) + "' not found");
			}
			return noCollection();
		}

		// Put the relevant objects in the model, depending
		// of the view requested
		ViewTypes vt;
		try {
			vt = ViewTypes.valueOf(FilenameUtils.getExtension(request.getRequestURI()));
		} catch (IllegalArgumentException iae) {
			log.warn("Search on collection '" + question.getCollection().getId()
					+ "' called with an unknown extension '"+request.getRequestURI()+"'.");
			throw new ViewTypeNotFoundException(FilenameUtils.getExtension(request.getRequestURI()));
		}
		
		Map<String, Object> model = getModel(vt, request, transaction);

		// Generate the view name, relative to the Funnelback home
		String viewName = DefaultValues.FOLDER_CONF + "/"
			+ question.getCollection().getId()	+ "/"
			+ question.getProfile() + "/"
			+ question.getForm();
		log.debug("Selected view '" + viewName + "'");
		
		return new ModelAndView(viewName, model);

	}
	
	private Map<String, Object> getModel(ViewTypes vt, HttpServletRequest request, SearchTransaction st) {
		Map<String, Object> out = new HashMap<String, Object>();
		switch (vt) {
		case json:
		case html:
		case htm:
			out.put(ModelAttributes.question.toString(), st.getQuestion());
			out.put(ModelAttributes.response.toString(), st.getResponse());
			out.put(ModelAttributes.error.toString(), st.getError());
			if (st.getExtraSearches().size() > 0) {
				out.put(ModelAttributes.extra.toString(), st.getExtraSearches());
			}
			out.put(ModelAttributes.QueryString.toString(), request.getQueryString());
			break;
		case xml:
		case legacy:
		default:
			out.put(ModelAttributes.SearchTransaction.toString(), st);
		}
		
		return out;
	}
	
	@ExceptionHandler(ViewTypeNotFoundException.class)
	public void viewTypeNotFound(HttpServletResponse response) throws IOException {
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	}
	
}
