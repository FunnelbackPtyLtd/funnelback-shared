package com.funnelback.contentoptimiser.fetchers.impl;

import java.io.File;
import java.io.IOException;

import lombok.extern.apachecommons.Log;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.common.utils.cgirunner.CgiRunner;
import com.funnelback.common.utils.cgirunner.CgiRunnerException;
import com.funnelback.common.utils.cgirunner.DefaultCgiRunner;
import com.funnelback.contentoptimiser.UrlStatus;
import com.funnelback.contentoptimiser.fetchers.UrlStatusFetcher;
import com.funnelback.publicui.search.service.ConfigRepository;

@Log
@Component
public class DefaultUrlStatusFetcher implements UrlStatusFetcher {

	@Autowired
	File searchHome;
	
	@Autowired
	ConfigRepository configRepository;
	

	@Override
	public UrlStatus fetch(String optimiserUrl, String collection) {

		UrlStatus status = null;
		
		File perlBin = new File(configRepository.getExecutablePath(Keys.Executables.PERL));
		CgiRunner runner = new DefaultCgiRunner(
				new File(searchHome, DefaultValues.FOLDER_WEB + File.separator +  DefaultValues.FOLDER_ADMIN  + File.separator + "url-status.cgi"),
				perlBin)
			.addRequestParameter("u", optimiserUrl)
			.addRequestParameter("c", collection)		
			.addRequestParameter("f", "json")
			.setTaint()
			.setEnvironmentVariable("REMOTE_USER", "admin")
			.setEnvironmentVariable("SEARCH_HOME", searchHome.getAbsolutePath());
		String json = "";
		try {
			json = runner.runToString();
		} catch (CgiRunnerException e1) {
			log.error("Unable to run the CGI command to get URL status ",e1);
			return status;
		}
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			// strip header
			json = json.replaceFirst("(?s).*Content-Type: application/json", "");
			status = mapper.readValue(json, UrlStatus.class);
		} catch (JsonParseException e) {
			log.error("Error obtaining info about URL",e);
		} catch (JsonMappingException e) {
			log.error("Error obtaining info about URL",e);
		} catch (IOException e) {
			log.error("Error obtaining info about URL",e);
		}
		
		return status;
	}

}

