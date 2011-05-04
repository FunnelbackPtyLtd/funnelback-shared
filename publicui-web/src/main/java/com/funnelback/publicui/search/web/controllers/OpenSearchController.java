package com.funnelback.publicui.search.web.controllers;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.web.binding.CollectionEditor;

@Controller
public class OpenSearchController {

	@Autowired
	private ConfigRepository configRepository;

	@InitBinder
	public void initBinder(DataBinder binder) {
		binder.registerCustomEditor(Collection.class, new CollectionEditor(configRepository));
	}

	
	@RequestMapping("/open-search.xml")
	public ModelAndView openSearch(Collection collection, HttpServletRequest request, HttpServletResponse response) {
		if (collection != null) {
			Map<String, String> model = new HashMap<String, String>();
			model.put("serviceName", collection.getConfiguration().value(Keys.SERVICE_NAME));
			model.put("name", collection.getId());
			model.put("host", request.getLocalName());
			model.put("searchUrl", "dummy");
			return new ModelAndView("open-search", model);
		} else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
	}
	
}
