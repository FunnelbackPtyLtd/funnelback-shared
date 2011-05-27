package com.funnelback.publicui.search.web.controllers;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.freemarker.FreeMarkerView;

@Controller
public class ContentOptimiserController {

	@Resource(name="contentOptimiserKickoffView")
	FreeMarkerView contentOptimiserKickoffView; 
	
	@RequestMapping(value="/content-optimiser-kickoff.html")
	public View kickoff() {
		return contentOptimiserKickoffView;
	}
}
