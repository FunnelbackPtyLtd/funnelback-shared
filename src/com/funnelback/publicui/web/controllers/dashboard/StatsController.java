package com.funnelback.publicui.web.controllers.dashboard;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/dashboard/stats/")
public class StatsController {

	@Resource(name="profilingStats")
	private Map<String, LinkedList<Long>> stats;

	@RequestMapping("list")
	public ModelAndView list() {
		
		Map<String, Map<String, LinkedList<Long>>> model = new HashMap<String, Map<String, LinkedList<Long>>>();
		model.put("statistics", stats);
		return new ModelAndView("/dashboard/stats/list", model);
	}
	
	@RequestMapping("{statsId}/show")
	public ModelAndView show(@PathVariable String statsId) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("statsId", statsId);
		model.put("statistics", stats.get(statsId));
		return new ModelAndView("/dashboard/stats/show", model);
	}
	
}
