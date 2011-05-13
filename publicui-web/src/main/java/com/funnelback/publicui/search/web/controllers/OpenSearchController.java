package com.funnelback.publicui.search.web.controllers;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.freemarker.FreeMarkerView;

import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.service.ConfigRepository;

/**
 * Generate an OpenSearchDescription XML snippet
 * for a given collection. 
 */
@Controller
public class OpenSearchController {

	private static final String URI = "open-search.xml";
	
	@Autowired
	private ConfigRepository configRepository;
	
	@Resource(name="openSearchView")
	private FreeMarkerView openSearchView;
	
	

	@RequestMapping(value="/"+URI,params=RequestParameters.COLLECTION)
	public ModelAndView openSearch(HttpServletRequest request, HttpServletResponse response) {
		Collection collection = configRepository.getCollection(request.getParameter(RequestParameters.COLLECTION));
		if (collection != null) {
			Map<String, String> model = new HashMap<String, String>();
			model.put("serviceName", collection.getConfiguration().value(Keys.SERVICE_NAME));
			model.put("name", collection.getId());
			model.put("host", request.getHeader("Host"));
			model.put("searchUrl", buildSearchUrl(request, collection));
			return new ModelAndView(openSearchView, model);
		} else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
	}
	
	private String buildSearchUrl(HttpServletRequest request, Collection collection) {
		return new StringBuffer()
		.append(request.getRequestURL().toString().replace(URI, collection.getConfiguration().value(Keys.PublicUI.SEARCH_LINK)))
		.append("?collection=" + collection.getId())
		.append("&amp;query={searchTerms}").toString();
		
	}
	
}
