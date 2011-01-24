package com.funnelback.publicui.web.controllers.dashboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Statistics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller("dashboardCacheController")
@RequestMapping("/dashboard/caches/")
public class CacheController {

	@Autowired
	private CacheManager appCacheManager;
	
	@RequestMapping("list")
	public ModelAndView showCaches() {
		
		List<Statistics> cacheStatistics = new ArrayList<Statistics>();
		
		for(String cache : appCacheManager.getCacheNames()) {
			cacheStatistics.add(appCacheManager.getCache(cache).getStatistics());
		}

		Map<String, List<Statistics>> model = new HashMap<String, List<Statistics>>();
		model.put("statistics", cacheStatistics);
		return new ModelAndView("/dashboard/caches/list", model);
	}
	
	@RequestMapping("{cacheName}/flush/")
	public String flushCache(@PathVariable String cacheName) {
		appCacheManager.getCache(cacheName).flush();
		return "redirect:/dashboard/caches/list";
	}
	
	@RequestMapping("{cacheName}/remove/{key}/")
	public String removeElement(@PathVariable String cacheName, @PathVariable String key) {
		appCacheManager.getCache(cacheName).remove(key);
		return "redirect:/dashboard/caches/list";
	}
	
}
