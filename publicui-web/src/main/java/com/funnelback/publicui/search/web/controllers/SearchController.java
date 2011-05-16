package com.funnelback.publicui.search.web.controllers;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.freemarker.FreeMarkerView;

import waffle.servlet.WindowsPrincipal;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.contentoptimiser.DefaultUrlCauseFiller;
import com.funnelback.contentoptimiser.UrlCausesFiller;
import com.funnelback.contentoptimiser.UrlComparison;
import com.funnelback.publicui.search.lifecycle.SearchTransactionProcessor;
import com.funnelback.publicui.search.lifecycle.input.processors.PassThroughEnvironmentVariables;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.log.LogUtils;
import com.funnelback.publicui.search.web.binding.CollectionEditor;
import com.funnelback.publicui.utils.MapKeyFilter;
import com.funnelback.publicui.utils.MapUtils;
import com.funnelback.publicui.xml.XmlParsingException;

//@RequestMapping({"/search.*", "/_/search.*","/search/"})
@Controller
@lombok.extern.apachecommons.Log
public class SearchController {

	public enum ModelAttributes {
		SearchTransaction, AllCollections, QueryString, SearchPrefix, ContextPath,
		question, response, error;
		
		public static Set<String> getNames() {
			HashSet<String> out = new HashSet<String>();
			for (ModelAttributes name: values()) {
				out.add(name.toString());
			}
			return out;
		}
	}
	
	private enum ViewTypes {
		html, xml, json, legacy;
	}

	@Autowired
	private SearchTransactionProcessor processor;
	
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
		model.put(ModelAttributes.AllCollections.toString(), configRepository.getAllCollections());

		// FIXME: Hack for the XML view that serialize only one item from the model
		model.put(ModelAttributes.SearchTransaction.toString(), configRepository.getAllCollections());

		return new ModelAndView("no-collection", model);
	}
	
	private UrlCausesFiller filler = new DefaultUrlCauseFiller();
	

	@Resource(name="contentOptimiserView")
	private FreeMarkerView contentOptimiserView;
	
	@RequestMapping(value="/content-optimiser.html")
	public ModelAndView contentOptimiser(HttpServletRequest request, SearchQuestion question) throws IOException, XmlParsingException {
		UrlComparison comparison = new UrlComparison();
		
		ModelAndView search = search(request, question);
		
		Map<String, Object> model = search.getModel();
		SearchResponse searchResponse = ((SearchResponse)model.get(ModelAttributes.response.toString()));
		
		ResultPacket resultPacket = searchResponse.getResultPacket();
		filler.consumeResultPacket(comparison,resultPacket);		
		filler.setImportantUrl("",comparison,resultPacket);
		
		filler.fillHints(comparison);
		
		//Map<String,Object> m = new HashMap<String,Object>();
		//log.debug(url1 + " " + url2 );
		model.put("explanation",comparison);
		
		return new ModelAndView(contentOptimiserView,model);
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
			SearchQuestion question) {
				
		SearchTransaction transaction = null;
		
		additionalDataBinding(question, request);
		
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
		Map<String, Object> model = getModel(ViewTypes.valueOf(FilenameUtils.getExtension(request.getRequestURI())), request, transaction);

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

		// Parameter map
		question.getInputParameterMap().putAll(request.getParameterMap());
	
		// Add any HTTP servlet specifics 
		MapUtils.putIfNotNull(question.getInputParameterMap(), PassThroughEnvironmentVariables.Keys.REMOTE_ADDR.toString(), request.getRemoteAddr());
		MapUtils.putIfNotNull(question.getInputParameterMap(), PassThroughEnvironmentVariables.Keys.REQUEST_URI.toString(), request.getRequestURI());
		MapUtils.putIfNotNull(question.getInputParameterMap(), PassThroughEnvironmentVariables.Keys.AUTH_TYPE.toString(), request.getAuthType());
		MapUtils.putIfNotNull(question.getInputParameterMap(), PassThroughEnvironmentVariables.Keys.HTTP_HOST.toString(), request.getHeader("host"));
		MapUtils.putIfNotNull(question.getInputParameterMap(), PassThroughEnvironmentVariables.Keys.REMOTE_USER.toString(), request.getRemoteUser());

		// Referer
		MapUtils.putIfNotNull(question.getInputParameterMap(), PassThroughEnvironmentVariables.Keys.HTTP_REFERER.toString(), request.getHeader("Referer"));
				
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
		MapKeyFilter filter = new MapKeyFilter(request.getParameterMap());
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
	
	private Map<String, Object> getModel(ViewTypes vt, HttpServletRequest request, SearchTransaction st) {
		Map<String, Object> out = new HashMap<String, Object>();
		switch (vt) {
		case json:
		case html:
			out.put(ModelAttributes.question.toString(), st.getQuestion());
			out.put(ModelAttributes.response.toString(), st.getResponse());
			out.put(ModelAttributes.error.toString(), st.getError());
			out.put(ModelAttributes.QueryString.toString(), request.getQueryString());
			break;
		case xml:
		case legacy:
		default:
			out.put(ModelAttributes.SearchTransaction.toString(), st);
		}
		
		return out;
	}
}
