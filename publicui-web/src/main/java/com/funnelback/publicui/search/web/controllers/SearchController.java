package com.funnelback.publicui.search.web.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import lombok.extern.log4j.Log4j;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.LocaleResolver;
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
import com.funnelback.publicui.search.web.binding.StringArrayFirstSlotEditor;
import com.funnelback.publicui.search.web.exception.ViewTypeNotFoundException;

/**
 * <p>Main controller for the Modern UI.</p>
 * 
 * <ul>
 * 	<li>Deal with special cases like the collection list page</li>
 *  <li>Processes input parameters</li>
 *  <li>Call the transaction processor to process the search</li>
 *  <li>Select the correct view (HTML, JSON, XML ...)</li>
 * </ul>
 *
 */
@Controller
@Log4j
public class SearchController {

	public enum ModelAttributes {
		SearchTransaction, AllCollections, QueryString, SearchPrefix, ContextPath, Log,
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
		html, htm, xml, json, classic;
	}

	@Autowired
	private SearchTransactionProcessor processor;
	
	@Autowired
	private ConfigRepository configRepository;
	
	@Autowired
	private LocaleResolver localeResolver;

	/**
	 * <p>Configures the binder to:
	 * <ul>
	 * 	<li>Restrict which URL parameters can be mapped to Java objects</li>
	 * 	<li>Convert a collection ID into a proper collection object</li>
	 * </ul>
	 * </p>
	 * @param binder
	 * @see http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/html/mvc.html#mvc-ann-webdatabinder
	 */
	@InitBinder
	public void initBinder(DataBinder binder) {
		// For security reasons, only allow specific fields for
		// data binding
		binder.setAllowedFields(
				RequestParameters.CLIVE,
				RequestParameters.COLLECTION,
				RequestParameters.FORM,
				RequestParameters.PROFILE,
				RequestParameters.QUERY	);
		binder.registerCustomEditor(Collection.class, new CollectionEditor(configRepository));
		binder.registerCustomEditor(String.class, RequestParameters.PROFILE, new StringArrayFirstSlotEditor());
	}
	
	@RequestMapping(value={"/"})
	public String index() {
		return "redirect:/search.html";
	}
	
	/**
	 * Called when no collection has been specified.
	 * @return a list of all available collections.
	 */
	@RequestMapping(value={"/search.html"},params="!"+RequestParameters.COLLECTION)
	public ModelAndView noCollection(HttpServletResponse response) {
		return noCollection(response, HttpStatus.BAD_REQUEST);
	}
	
	/**
	 * Helper method to return the collection list with a given status
	 * @param response 
	 * @param status Status code to return
	 * @return
	 */
	private ModelAndView noCollection(HttpServletResponse response, HttpStatus status) {
		if (status != null) {
			response.setStatus(status.value());
		}
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(ModelAttributes.AllCollections.toString(), configRepository.getAllCollections());

		return new ModelAndView(DefaultValues.FOLDER_WEB+"/"
				+DefaultValues.FOLDER_TEMPLATES+"/"
				+DefaultValues.FOLDER_MODERNUI+"/no-collection", model);
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
			HttpServletResponse response,
			@Valid SearchQuestion question) {
				
		SearchTransaction transaction = null;
		SearchQuestionBinder.bind(request, question, localeResolver);
		
		if (question.getCollection() != null) {
			// This is were the magic happens. The TransactionProcessor
			// will take care of processing the search request.
			transaction = processor.process(question);
		} else {
			// Collection is null = non existent
			if (request.getParameter(SearchQuestion.RequestParameters.COLLECTION) != null) {
				log.warn("Collection '" + request.getParameter(SearchQuestion.RequestParameters.COLLECTION) + "' not found");
			}
			return noCollection(response, HttpStatus.NOT_FOUND);
		}
		
		if (transaction.getError() != null) {
			// Error occured while processing the transaction, set the
			// response status code accordingly
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
		case classic:
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
