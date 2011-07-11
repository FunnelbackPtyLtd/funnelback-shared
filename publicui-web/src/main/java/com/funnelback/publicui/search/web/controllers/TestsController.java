package com.funnelback.publicui.search.web.controllers;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import lombok.Setter;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.UrlResource;
import org.springframework.scripting.groovy.GroovyScriptFactory;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@lombok.extern.apachecommons.Log
// @RequestMapping({"/test", "/_/test"})
@RequestMapping("/")
public class TestsController implements ApplicationContextAware {
	
	/** Needed to instantiate Spring resource request handler */
	@Setter private ApplicationContext applicationContext;
	
	@RequestMapping("hello")
	public void facetedNav(HttpServletResponse response) throws IOException {
		response.setContentType("text/plain");
		response.getWriter().write("hello");
	}
	
	@RequestMapping("groovy")
	public void groovy(HttpServletResponse response) throws IOException {

		GroovyScriptFactory f = new GroovyScriptFactory("C:/Temp/trash/TestTransform.groovy");
		Object script = f.getScriptedObject(new ResourceScriptSource(new UrlResource("file:///C:/Temp/trash/TestTransform.groovy")), null);
		
		System.out.println("yes: " + script);
	
		
	}
	
}
