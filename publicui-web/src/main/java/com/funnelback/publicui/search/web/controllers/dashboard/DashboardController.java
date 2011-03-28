package com.funnelback.publicui.search.web.controllers.dashboard;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DashboardController {

	@RequestMapping("/dashboard/")
	public String index() {
		return "/dashboard/index";
	}
}
