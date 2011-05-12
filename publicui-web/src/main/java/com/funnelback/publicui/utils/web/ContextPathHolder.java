package com.funnelback.publicui.utils.web;

import javax.servlet.ServletContext;

import lombok.Getter;
import lombok.extern.apachecommons.Log;

import org.springframework.web.context.ServletContextAware;

/**
 * Holds a context path (such as /publicui)
 */
@Log
public class ContextPathHolder implements ServletContextAware {

	@Getter private String contextPath;
	
	@Override
	public void setServletContext(ServletContext servletContext) {
		this.contextPath = servletContext.getContextPath();
		log.info("Context path is '" + contextPath + "'");
	}

}
