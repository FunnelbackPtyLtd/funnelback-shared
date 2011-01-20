package com.funnelback.publicui.web.controllers;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.funnelback.publicui.search.model.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.DataRepository;
import com.funnelback.publicui.web.binding.CollectionEditor;

/**
 * Deal with cached copies
 */
@Controller
public class CacheController {

	@Autowired
	private DataRepository dataRepository;
	
	@Autowired
	private ConfigRepository configRepository;
	
	@InitBinder
	public void initBinder(DataBinder binder) {
		binder.registerCustomEditor(Collection.class, new CollectionEditor(configRepository));
	}

	@RequestMapping(value="/cache", method=RequestMethod.GET)
	public void cache(HttpServletResponse response,
			@RequestParam(RequestParameters.COLLECTION) Collection collection,
			String doc) throws IOException {
		
		String content = dataRepository.getCachedDocument(collection, doc);
		if (content != null) {
			// TODO For now output as text/plain
			response.setContentType("text/plain");
			response.getWriter().write(content);
		} else {
			// Cached copy unavailable
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
		}
	}
	
}
