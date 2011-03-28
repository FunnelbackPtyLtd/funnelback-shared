package com.funnelback.publicui.search.web.controllers.dashboard;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.publicui.aop.ProfiledAspect.MethodStats;

@Controller
@RequestMapping("/dashboard/stats/")
public class StatsController {

	@Resource(name="profilingStatsNotSynchronized")
	private Map<String, MethodStats> stats;

	@RequestMapping("list")
	public ModelAndView list() {
		Map<String, Map<String, MethodStats>> model = new HashMap<String, Map<String, MethodStats>>();
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
