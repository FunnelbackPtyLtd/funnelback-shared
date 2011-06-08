package com.funnelback.publicui.search.web.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.freemarker.FreeMarkerView;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.web.binding.CollectionEditor;
import com.funnelback.publicui.search.web.controllers.SearchController.ModelAttributes;
import com.funnelback.publicui.xml.XmlParsingException;

@Controller
public class ContentOptimiserController {

	@Autowired
	private ConfigRepository configRepository;
	
	@Resource(name="contentOptimiserKickoffView")
	FreeMarkerView contentOptimiserKickoffView; 

	@Resource(name="contentOptimiserView")
	private FreeMarkerView contentOptimiserView;
	
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
	@RequestMapping(value="/content-optimiser-kickoff.html",params="!"+RequestParameters.COLLECTION)
	public ModelAndView noCollectionKickoff() {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(ModelAttributes.AllCollections.toString(), configRepository.getAllCollections());

		return new ModelAndView("no-collection", model);
	}
	
	
	@RequestMapping(value="/content-optimiser.html",params={RequestParameters.COLLECTION,RequestParameters.QUERY})
	public ModelAndView contentOptimiser(HttpServletRequest request, SearchQuestion question) throws IOException, XmlParsingException {
	/*	if(question.getInputParameterMap().get(RequestParameters.QUERY) != null && ("").equals(question.getInputParameterMap().get(RequestParameters.QUERY)[0]) ) {
			return kickoff(request); 
		}*/
		question.getInputParameterMap().put(RequestParameters.EXPLAIN, new String[] {"on"});
		question.getInputParameterMap().put(RequestParameters.NUM_RANKS, new String[] {"999"});
		
		return new ModelAndView(contentOptimiserView, searchController.search(request, question).getModel());
	}
	

	@RequestMapping(value="/content-optimiser.html",params={RequestParameters.COLLECTION,"!"+RequestParameters.QUERY})
	public ModelAndView collectionNoQuery(HttpServletRequest request) {
			return kickoff(request);
	}
	
	@RequestMapping(value="/content-optimiser.html") 
	public ModelAndView noCollectionContentOptimiser(){
		return noCollectionKickoff();
	}
	
	//@RequestMapping(value="/content-optimiser.html",params={RequestParameters.COLLECTION,RequestParameters.QUERY})
	@RequestMapping(value={"/content-optimiser-kickoff.html"})
	public ModelAndView kickoff(HttpServletRequest request) {
		Map<String,Object> m = new HashMap<String,Object>();
		m.put("collection", request.getParameter(RequestParameters.COLLECTION));
		return new ModelAndView(contentOptimiserKickoffView,m);
	}
	

}
