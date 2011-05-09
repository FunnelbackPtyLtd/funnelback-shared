package com.funnelback.publicui.dashboard.web;

import javax.servlet.ServletContext;

import lombok.Getter;
import lombok.extern.apachecommons.Log;

import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

@Log
@Component("contextPathHolder")
public class ContextPathHolder implements ServletContextAware {

	@Getter private String contextPath;
	
	@Override
	public void setServletContext(ServletContext servletContext) {
		this.contextPath = servletContext.getContextPath();
		log.info("Context path is '" + contextPath + "'");
	}

}
