package com.funnelback.contentoptimiser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.apachecommons.Log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Log
@Controller
public class Example {
	
	@Autowired
	private UrlCausesFiller filler;
	
	
	@RequestMapping("content-optimiser.*") 
	public Map hello(String url1, String url2,String number) throws IOException {
		UrlComparison comparison = new UrlComparison();
		
		for(int i = 0; i < Integer.parseInt(number); i++) {
			filler.addUrl(url1,comparison);
		}
		
		filler.setImportantUrl(url2,comparison);
		
		filler.FillCauses(comparison);
		

		
		Map<String,Object> m = new HashMap<String,Object>();
		//log.debug(url1 + " " + url2 );
		m.put("explanation",comparison);

		return m;
	}
	
	
}
