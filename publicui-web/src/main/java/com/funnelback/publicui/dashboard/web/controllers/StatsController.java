package com.funnelback.publicui.dashboard.web.controllers;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.publicui.aop.ProfiledAspect.MethodStats;

@Controller
@RequestMapping("/stats/")
public class StatsController {

	@Resource(name="profilingStatsNotSynchronized")
	private Map<String, MethodStats> stats;

	@Resource(name="searchStats")
	private Map<String, Long> searchStats;

	@RequestMapping("list")
	public String list() {
		return "/stats/list";
	}
	
	
	@RequestMapping("methods/list")
	public ModelAndView methodsList() {
		Map<String, Map<String, MethodStats>> model = new HashMap<String, Map<String, MethodStats>>();
		model.put("statistics", stats);
		return new ModelAndView("/stats/method/list", model);
	}
	
	@RequestMapping("methods/{statsId}/show")
	public ModelAndView methodsShow(@PathVariable String statsId) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("statsId", statsId);
		model.put("statistics", stats.get(statsId));
		return new ModelAndView("/stats/method/show", model);
	}
	
	@RequestMapping("search/list")
	public ModelAndView searchList() {
		Map<String, Map<String, Long>> model = new HashMap<String, Map<String, Long>>();
		model.put("statistics", searchStats);
		return new ModelAndView("/stats/search/list", model);
	}
	
}
