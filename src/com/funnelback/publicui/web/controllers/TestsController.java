package com.funnelback.publicui.web.controllers;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.ServletContextAware;

import com.sun.jna.platform.win32.Advapi32Util;

@Controller
@lombok.extern.apachecommons.Log
public class TestsController implements ServletContextAware {

	@RequestMapping("/test/auth")
	public void testAuth(HttpServletRequest request, HttpServletResponse response) throws IOException {
		log.debug(request.getUserPrincipal());

		Enumeration<String> names = request.getSession().getAttributeNames();
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			log.debug("SESSION [" + name + "] = '" + request.getSession().getAttribute(name) + "'");
		}
		log.debug("Native user name = " + Advapi32Util.getUserName());
		
	}

	@Override
	public void setServletContext(ServletContext servletContext) {
		
		
	}
}
