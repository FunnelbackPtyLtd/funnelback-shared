package com.funnelback.publicui.dashboard.web.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import lombok.extern.apachecommons.Log;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.funnelback.publicui.search.lifecycle.data.DataFetcher;
import com.funnelback.publicui.search.lifecycle.input.InputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessor;

@Controller
@RequestMapping("/settings/")
@Log
public class SettingsController implements ApplicationContextAware {

	@Resource(name="inputFlow")
	private List<InputProcessor> inputFlow;
	
	@Resource(name="dataFetchers")
	private List<DataFetcher> dataFetchers;

	@Resource(name="outputFlow")
	private List<OutputProcessor> outputFlow;
	
	private ConfigurableApplicationContext context;
	
	@RequestMapping("list")
	public ModelAndView list() {
		Map<String, Object> model = new HashMap<String, Object>();
		
		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
		for (InputProcessor processor: inputFlow) {
			classes.add(AopUtils.getTargetClass(processor));
		}
		model.put("inputFlow", classes);

		classes = new ArrayList<Class<?>>();
		for (DataFetcher fetcher: dataFetchers) {
			classes.add(AopUtils.getTargetClass(fetcher));
		}
		model.put("dataFetchers", classes);
		
		classes = new ArrayList<Class<?>>();
		for (OutputProcessor processor: outputFlow) {
			classes.add(AopUtils.getTargetClass(processor));
		}
		model.put("outputFlow", classes);		
		
		return new ModelAndView("/settings/list", model);
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
	
	@RequestMapping("{type}/add")
	public String add(@PathVariable String type, @RequestParam String clazz, @RequestParam int index) {
		log.debug("Adding a '" + clazz + "' at index " + index);
		
		try {
			Object o = context.getBeanFactory().autowire(Class.forName(clazz), AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true);
			if ("input".equals(type)) {
				inputFlow.add(index, (InputProcessor) o);
			} else if ("data".equals(type)) {
				dataFetchers.add(index, (DataFetcher) o);
			} else if ("output".equals(type)) {
				outputFlow.add(index, (OutputProcessor) o);
			}
		} catch (Exception ex) {
			log.error(ex);
		}

		
		return "redirect:/dashboard/settings/list";
	}

	@Override
	public void setApplicationContext(ApplicationContext ctx) throws BeansException {
		context = (ConfigurableApplicationContext) ctx;
	}
}
