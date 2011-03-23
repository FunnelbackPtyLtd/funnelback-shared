package com.funnelback.publicui.web.controllers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import waffle.servlet.WindowsPrincipal;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.lifecycle.data.DataFetchException;
import com.funnelback.publicui.search.lifecycle.data.DataFetcher;
import com.funnelback.publicui.search.lifecycle.input.InputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchError;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.log.LogUtils;
import com.funnelback.publicui.web.binding.CollectionEditor;
import com.funnelback.publicui.web.utils.RequestParametersFilter;

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
	 * Default handler when we have a query and a collection.
	 * @param request
	 * @param question
	 * @return
	 */
	@RequestMapping(params={RequestParameters.COLLECTION})
	public ModelAndView search(
			HttpServletRequest request,
			@ModelAttribute SearchQuestion question) {
				
		SearchTransaction transaction = null;
		
		additionalDataBinding(question, request);
		
		if (question.getCollection() != null) {
			if (question.getQuery() != null && ! "".equals(question.getQuery())) {
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
	
				} catch (InputProcessorException ipe) {
					log.error(ipe);
					transaction.setError(new SearchError(SearchError.Reason.InputProcessorError, ipe));
				} catch (DataFetchException dfe) {
					log.error(dfe);
					transaction.setError(new SearchError(SearchError.Reason.DataFetchError, dfe));
				} catch (OutputProcessorException ope) {
					log.error(ope);
					transaction.setError(new SearchError(SearchError.Reason.OutputProcessorError, ope));
				}
			} else {
				// Query is null
				transaction = new SearchTransaction(question, null);
			}
		} else {
			// Collection is null = non existent
			log.warn("Collection '" + request.getParameter(SearchQuestion.RequestParameters.COLLECTION) + "' not found");
			transaction = new SearchTransaction(null, null);
			transaction.setError(new SearchError(SearchError.Reason.InvalidCollection, request.getParameter(SearchQuestion.RequestParameters.COLLECTION)));
			
			//FIXME What to do if no collection ?
			Map<String, Object> model = new HashMap<String, Object>();
			model.put(MODEL_KEY_SEARCH_TRANSACTION, transaction);

			return new ModelAndView("no-collection", model);
		}

		Map<String, Object> model = new HashMap<String, Object>();
		model.put(MODEL_KEY_SEARCH_TRANSACTION, transaction);

		/*
		String uri = request.getRequestURI();
		String viewSuffix = uri.substring(uri.indexOf(".")+1);
		String viewName;
		if ("xml".equals(viewSuffix) || "json".equals(viewSuffix)
				|| "legacy".equals(viewSuffix)) {
			viewName = viewSuffix + "View";
		} else {
			viewName = "conf/" + question.getCollection().getId()	+ ((question.getProfile() != null) ? "/"+question.getProfile() : "") + "/simple";
		}
		log.debug("Selecting view '" + viewName + "'");
		*/
		// Generate the view name, relative to the Funnelback home
		String viewName = DefaultValues.FOLDER_CONF + "/"
			+ question.getCollection().getId()	+ "/"
			+ question.getProfile() + "/"
			+ question.getForm();
		log.debug("Selected view '" + viewName + "'");
		
		return new ModelAndView(viewName, model);

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
	public boolean isRequestImpersonated(HttpServletRequest request) {
		return request.getUserPrincipal() != null && request.getUserPrincipal() instanceof WindowsPrincipal;
	}
	
	/**
	 * FIXME Workaround the fact that there is no way to do custom databinding with Spring MVC 3
	 * @param question
	 * @param request
	 */
	private void additionalDataBinding(SearchQuestion question, HttpServletRequest request) {

		// Referer
		question.setReferer((request.getHeader("Referer") != null) ? request.getHeader("Referer") : null);
		
		// Query string
		question.setQueryString(request.getQueryString());
		
		// Copy original query
		question.setOriginalQuery(question.getQuery());
		
		// Is request impersonated ?
		question.setImpersonated(isRequestImpersonated(request));
		
		// User identifier
		if (question.getCollection() != null && question.getCollection().getConfiguration() != null) {
			question.setUserId(LogUtils.getUserIdentifier(request,
					DefaultValues.UserIdToLog.valueOf(question.getCollection().getConfiguration().value(Keys.USERID_TO_LOG))));
		}
		
		// Last clicked cluster
		question.setCnClickedCluster(request.getParameter(RequestParameters.ContextualNavigation.CN_CLICKED));
		
		// Previously clicked clusters
		RequestParametersFilter filter = new RequestParametersFilter(request);
		String[] paramNames = filter.filter(RequestParameters.ContextualNavigation.CN_PREV_PATTERN);
		Arrays.sort(paramNames);
		for(String paramName : paramNames) {
			// We don't really care of the indexes given in parameter names
			String value = request.getParameter(paramName);
			if (value != null && !"".equals(value) ) {
				question.getCnPreviousClusters().add(value);
			}
		}
	}
}
