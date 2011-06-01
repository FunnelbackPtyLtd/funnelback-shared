package com.funnelback.publicui.search.web.controllers;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.freemarker.FreeMarkerView;

import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.web.controllers.SearchController.ModelAttributes;

@Controller
public class ContentOptimiserController {

	@Autowired
	private ConfigRepository configRepository;
	
	@Resource(name="contentOptimiserKickoffView")
	FreeMarkerView contentOptimiserKickoffView; 
	
	/**
	 * Called when no collection has been specified.
	 * @return a list of all available collections.
	 */
	@RequestMapping(value="/content-optimiser-kickoff.html",params="!"+RequestParameters.COLLECTION)
	public ModelAndView noCollection() {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(ModelAttributes.AllCollections.toString(), configRepository.getAllCollections());

		return new ModelAndView("no-collection", model);
	}
	
	//@RequestMapping(value="/content-optimiser.html",params={RequestParameters.COLLECTION,RequestParameters.QUERY})
	@RequestMapping(value={"/content-optimiser-kickoff.html"})
	public ModelAndView kickoff(HttpServletRequest request) {
		Map<String,Object> m = new HashMap<String,Object>();
		m.put("collection", request.getParameter(RequestParameters.COLLECTION));
		return new ModelAndView(contentOptimiserKickoffView,m);
	}
	

}
