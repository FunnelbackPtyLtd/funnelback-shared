package com.funnelback.publicui.search.web.controllers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import lombok.SneakyThrows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.freemarker.FreeMarkerView;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.contentoptimiser.ContentOptimiserUserRestrictions;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.web.binding.CollectionEditor;
import com.funnelback.publicui.search.web.controllers.SearchController.ModelAttributes;
import com.funnelback.publicui.xml.XmlParsingException;

@Controller
@RequestMapping("/content-optimiser")
public class ContentOptimiserController {

	@Autowired
	private ConfigRepository configRepository;
	
	@Resource(name="contentOptimiserKickoffView")
	private FreeMarkerView contentOptimiserKickoffView; 

	@Resource(name="contentOptimiserView")
	private FreeMarkerView contentOptimiserView;
	
	@Resource(name="contentOptimiserTextView")
	private FreeMarkerView contentOptimiserTextView;

	@Resource(name="contentOptimiserLoadingView")
	private FreeMarkerView contentOptimiserLoadingView;

	
	@Autowired
	private SearchController searchController;
	
	@InitBinder
	public void initBinder(DataBinder binder) {
		binder.registerCustomEditor(Collection.class, new CollectionEditor(configRepository));
	}
	
	/**
	 * Called when no collection has been specified.
	 * @return a list of all available collections.
	 */
	@RequestMapping(value={"/"},params="!"+RequestParameters.COLLECTION)
	public ModelAndView noCollectionKickoff() {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(ModelAttributes.AllCollections.toString(), configRepository.getAllCollections());

		return new ModelAndView(DefaultValues.FOLDER_WEB+"/"
				+DefaultValues.FOLDER_TEMPLATES+"/"
				+DefaultValues.FOLDER_MODERNUI+"/no-collection", model);
	}
	
	@RequestMapping(value="")
	public String noSlash() {
		return "redirect:content-optimiser/";
	}
	
	
	@RequestMapping(value="/optimise.html",params={RequestParameters.COLLECTION,RequestParameters.QUERY,RequestParameters.CONTENT_OPTIMISER_ADVANCED})
	public ModelAndView contentOptimiserAdvanced(HttpServletRequest request, SearchQuestion question) throws IOException, XmlParsingException {
		question.getInputParameterMap().put(RequestParameters.EXPLAIN, "on");
		question.getInputParameterMap().put(RequestParameters.NUM_RANKS, "999");
		if("".equals(question.getQuery())) {
			return kickoff(request);
		}

		ContentOptimiserUserRestrictions contentOptimiserUserRestrictions = (ContentOptimiserUserRestrictions)request.getAttribute(ContentOptimiserUserRestrictions.class.getName());
		Map<String, Object> model = searchController.search(request, question).getModel();
		
		if(contentOptimiserUserRestrictions.isAllowNonAdminFullAccess()) {
 			String nonAdminLink = question.getCollection().getConfiguration().value(Keys.Urls.SEARCH_PROTOCOL) + "://" + question.getCollection().getConfiguration().value(Keys.Urls.SEARCH_HOST) + ":" + question.getCollection().getConfiguration().value(Keys.Urls.SEARCH_PORT) + request.getRequestURI().replace("optimise.html", "runOptimiser.html") + "?" + request.getQueryString();
			model.put("nonAdminLink", nonAdminLink);
		}
		if(contentOptimiserUserRestrictions.isOnAdminPort()) model.put("onAdminPort", "true");
		
		return new ModelAndView(contentOptimiserView, model);
	}
	
	@RequestMapping(value="/optimise.html",params={RequestParameters.COLLECTION,RequestParameters.QUERY,"!advanced"})
	public ModelAndView contentOptimiserTextOnly(HttpServletRequest request, SearchQuestion question) throws IOException, XmlParsingException {
		question.getInputParameterMap().put(RequestParameters.EXPLAIN, "on");
		question.getInputParameterMap().put(RequestParameters.NUM_RANKS, "999");
		if("".equals(question.getQuery())) {
			return kickoff(request);
		}
		Map<String, Object> model = searchController.search(request, question).getModel();
		
		ContentOptimiserUserRestrictions contentOptimiserUserRestrictions = (ContentOptimiserUserRestrictions)request.getAttribute(ContentOptimiserUserRestrictions.class.getName());
	
		if(contentOptimiserUserRestrictions.isAllowNonAdminTextAccess()) {
 			String nonAdminLink = question.getCollection().getConfiguration().value(Keys.Urls.SEARCH_PROTOCOL) + "://" + question.getCollection().getConfiguration().value(Keys.Urls.SEARCH_HOST) + ":" + question.getCollection().getConfiguration().value(Keys.Urls.SEARCH_PORT) + request.getRequestURI().replace("optimise.html", "runOptimiser.html") + "?" + request.getQueryString();
			model.put("nonAdminLink", nonAdminLink);
		}
		if(contentOptimiserUserRestrictions.isOnAdminPort()) model.put("onAdminPort", "true");
		
		return new ModelAndView(contentOptimiserTextView, model);
	}
	

	@RequestMapping(value="/optimise.html",params={RequestParameters.COLLECTION,"!"+RequestParameters.QUERY})
	public ModelAndView collectionNoQuery(HttpServletRequest request) {
		return kickoff(request);
	}
	
	@RequestMapping(value="/optimise.html") 
	public ModelAndView noCollectionContentOptimiser(){
		return noCollectionKickoff();
	}
	
	//@RequestMapping(value="/content-optimiser.html",params={RequestParameters.COLLECTION,RequestParameters.QUERY})
	@RequestMapping(value={"/"})
	public ModelAndView kickoff(HttpServletRequest request) {
		Map<String,Object> m = new HashMap<String,Object>();
		m.put("collection", request.getParameter(RequestParameters.COLLECTION));
		if(request.getParameter(RequestParameters.CONTENT_OPTIMISER_ADVANCED) != null) m.put("advanced", request.getParameter(RequestParameters.CONTENT_OPTIMISER_ADVANCED));
		return new ModelAndView(contentOptimiserKickoffView,m);
	}
	
	@RequestMapping(value="/runOptimiser.html")
	@SneakyThrows(UnsupportedEncodingException.class)
	public ModelAndView loadingScreen(HttpServletRequest request) {
		Map<String, Object> model = new HashMap<String,Object>();
		
		StringBuilder sb = new StringBuilder();
		sb.append("optimise.html?");
	    for (Enumeration<String> e = request.getParameterNames() ; e.hasMoreElements() ;) {
	    	String key = e.nextElement();
	        sb.append(key);
	    	sb.append("=");
	    	sb.append(URLEncoder.encode(request.getParameter(key),"UTF-8"));
	    	sb.append("&");
	    }
	    model.put("urlToLoad", sb.toString());
		return new ModelAndView(contentOptimiserLoadingView,model);
	}

}
