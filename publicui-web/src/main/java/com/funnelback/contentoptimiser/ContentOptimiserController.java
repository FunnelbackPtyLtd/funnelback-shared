package com.funnelback.contentoptimiser;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.apachecommons.Log;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.xml.impl.StaxStreamParser;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.xml.XmlParsingException;

@Log
@Controller
public class ContentOptimiserController {
	
	@Autowired
	private UrlCausesFiller filler;
	
	
	@RequestMapping("content-optimiser.*") 
	public Map contentOptimiser(String url1, String url2,String number) throws IOException, XmlParsingException {
		UrlComparison comparison = new UrlComparison();
		
		StaxStreamParser parser = new StaxStreamParser();
		ResultPacket rp = parser.parse(FileUtils.readFileToString(new ClassPathResource("explain-mockup.xml").getFile(), "UTF-8"));
		
		filler.consumeResultPacket(comparison,rp);		
		filler.setImportantUrl(url2,comparison);
		
		filler.fillHints(comparison);
		

		
		Map<String,Object> m = new HashMap<String,Object>();
		//log.debug(url1 + " " + url2 );
		m.put("explanation",comparison);

		return m;
	}
	
	
}
