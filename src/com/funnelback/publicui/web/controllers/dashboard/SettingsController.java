package com.funnelback.publicui.web.controllers.dashboard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import lombok.extern.apachecommons.Log;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.publicui.search.lifecycle.data.DataFetcher;
import com.funnelback.publicui.search.lifecycle.input.InputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessor;

@Controller
@RequestMapping("/dashboard/settings/")
@Log
public class SettingsController {

	@Resource(name="inputFlow")
	private List<InputProcessor> inputFlow;
	
	@Resource(name="dataFetchers")
	private List<DataFetcher> dataFetchers;

	@Resource(name="outputFlow")
	private List<OutputProcessor> outputFlow;
	
	@RequestMapping("list")
	public ModelAndView list() {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("inputFlow", inputFlow);
		model.put("dataFetchers", dataFetchers);
		model.put("outputFlow", outputFlow);
		return new ModelAndView("/dashboard/settings/list", model);
	}
	
	@RequestMapping("{type}/{index}/remove")
	public String remove(@PathVariable String type, @PathVariable int index) {
		if ("input".equals(type)) {
			inputFlow.remove(index);
		} else if ("data".equals(type)) {
			dataFetchers.remove(index);
		} else if ("output".equals(type)) {
			outputFlow.remove(index);
		}
		log.debug("Removed item " + index + " from '" + type + "' list");
		
		return "redirect:/dashboard/settings/list";
	}
}
