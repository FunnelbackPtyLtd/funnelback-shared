package com.funnelback.publicui.search.web.controllers.dashboard;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.service.ConfigRepository;

@Controller
@RequestMapping("/dashboard/collections/")
public class CollectionsController {
	
	@Autowired
	private ConfigRepository configRepository;
	
	@RequestMapping("list")
	public HashMap<String, List<Collection>> list() {
		HashMap<String, List<Collection>> model = new HashMap<String, List<Collection>>();
		model.put("collections", configRepository.getAllCollections());
		return model;
	}
	
	@RequestMapping("{id}/view")
	public ModelAndView view(@PathVariable String id) {
		HashMap<String, Object> model = new HashMap<String, Object>();
		model.put("collections", configRepository.getAllCollections());
		model.put("collection", configRepository.getCollection(id));
		return new ModelAndView("/dashboard/collections/view", model);
	}

}
