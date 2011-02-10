package com.funnelback.publicui.web.controllers;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@lombok.extern.apachecommons.Log
@RequestMapping({"/test", "/_/test"})
public class TestsController {
	
	@RequestMapping("hello")
	public void facetedNav(HttpServletResponse response) throws IOException {
		response.setContentType("text/plain");
		response.getWriter().write("hello");
	}
	
}
