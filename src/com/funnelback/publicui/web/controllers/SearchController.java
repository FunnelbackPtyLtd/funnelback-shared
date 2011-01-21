package com.funnelback.publicui.web.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import waffle.servlet.WindowsPrincipal;

import com.funnelback.publicui.search.lifecycle.data.DataFetchException;
import com.funnelback.publicui.search.lifecycle.data.DataFetcher;
import com.funnelback.publicui.search.lifecycle.input.InputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessor;
import com.funnelback.publicui.search.model.Collection;
import com.funnelback.publicui.search.model.transaction.SearchError;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.web.binding.CollectionEditor;

@Controller
@RequestMapping({"/search", "/_/search"})
@lombok.extern.apachecommons.Log
public class SearchController {

	public static final String MODEL_KEY_SEARCH_TRANSACTION = SearchTransaction.class.getSimpleName();
	public static final String MODEL_KEY_COLLECTION_LIST = "allCollections";

	// Can't use @Autowired for those 3 one otherwise
	// Spring will automatically construct a Set with all existing
	// implementations of InputProcessor, DataFetcher, OutputProcessor
	// ----
	@Resource(name="inputFlow")
	private List<InputProcessor> inputFlow;
	
	@Resource(name="dataFetchers")
	private List<DataFetcher> dataFetchers;

	@Resource(name="outputFlow")
	private List<OutputProcessor> outputFlow;
	// ----

	@Autowired
	private ConfigRepository configRepository;

	@InitBinder
	public void initBinder(DataBinder binder) {
		binder.registerCustomEditor(Collection.class, new CollectionEditor(configRepository));
	}
	
	/**
	 * Called when no collection has been specified.
	 * @return a list of all available collections.
	 */
	@RequestMapping(params="!"+RequestParameters.COLLECTION)
	public ModelAndView noCollection() {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(MODEL_KEY_COLLECTION_LIST, configRepository.getAllCollections());

		// FIXME: Hack for the XML view that serialize only one item from the model
		model.put(MODEL_KEY_SEARCH_TRANSACTION, configRepository.getAllCollections());

		return new ModelAndView("no-collection", model);
	}
	
	/**
	 * Called when no query has been specified.	
	 * @param collection
	 * @return
	 */
	@RequestMapping(params="!"+RequestParameters.QUERY)
	public ModelAndView noQuery(@RequestParam("collection") Collection collection) {
		SearchQuestion question = new SearchQuestion();
		question.setCollection(collection);
		
		SearchTransaction transaction = new SearchTransaction(question, null);
		
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(MODEL_KEY_SEARCH_TRANSACTION, transaction);
		return new ModelAndView("search", model);		
	}
	
	/**
	 * Default handler when we have a query and a collection.
	 * @param request
	 * @param question
	 * @return
	 */
	@RequestMapping(params={RequestParameters.COLLECTION, RequestParameters.QUERY})
	public ModelAndView search(
			HttpServletRequest request,
			@ModelAttribute SearchQuestion question) {
		
		SearchTransaction transaction = null;
		
		question.setImpersonated(isRequestImpersonated(request));
		
		if (question.getCollection() != null) {
			SearchResponse response = new SearchResponse();
			transaction = new SearchTransaction(question, response);
			try {
				for (InputProcessor processor : inputFlow) {
					processor.process(transaction, request);
				}

				for (DataFetcher fetcher : dataFetchers) {
					fetcher.fetchData(transaction);
				}

				for (OutputProcessor processor : outputFlow) {
					processor.process(transaction);
				}

			} catch (DataFetchException dfe) {
				log.error(dfe);
				transaction.setError(new SearchError(SearchError.Reason.DataFetchError, dfe));
			}

		} else {
			// Collection is null = non existent
			log.warn("Collection '" + request.getParameter(SearchQuestion.RequestParameters.COLLECTION) + "' not found");
			transaction = new SearchTransaction(null, null);
			transaction.setError(new SearchError(SearchError.Reason.InvalidCollection, request.getParameter(SearchQuestion.RequestParameters.COLLECTION)));
		}

		Map<String, Object> model = new HashMap<String, Object>();
		model.put(MODEL_KEY_SEARCH_TRANSACTION, transaction);

		return new ModelAndView("search", model);

	}

	/**
	 * Detects if the request is impersonated.
	 * 
	 * TODO The current implementation doesn't really check if the user is impersonated
	 * but only relies on the fact that it has been authenticated using the Waffle filter.
	 * Unfortunately J2EE 5 doesn't allow us to have access to the FilterConfig to read
	 * the value of the "impersonate" parameter (Possible with J2EE 6)
	 *   - Either switch to J2EE 6
	 *   - Or update the WindowsPrincipal with a impersonation status field ?
	 *   
	 * @param request
	 * @return true if the request is impersonated, false otherwise
	 */
	private boolean isRequestImpersonated(HttpServletRequest request) {
		return request.getUserPrincipal() != null && request.getUserPrincipal() instanceof WindowsPrincipal;
	}
	
	/*
 

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public Map<String, Object> handleMissingParameterException(MissingServletRequestParameterException msrpe,
			HttpServletRequest request) {

		SearchTransaction transaction = new SearchTransaction(null, null);
		transaction.setError(new SearchError(SearchError.Reason.MissingParameter, msrpe.getParameterName()));

		Map<String, Object> model = new HashMap<String, Object>();
		model.put(SearchController.MODEL_KEY_SEARCH_TRANSACTION, transaction);

		return model;
	}

	@ExceptionHandler(Exception.class)
	public Map<String, Object> handleConversionFailed(Exception ex) {
		SearchTransaction transaction = new SearchTransaction(null, null);
		transaction.setError(new SearchError(SearchError.Reason.Unknown, ex.getMessage()));

		Map<String, Object> model = new HashMap<String, Object>();
		model.put(SearchController.MODEL_KEY_SEARCH_TRANSACTION, transaction);

		return model;
	}
	
	*/
}
