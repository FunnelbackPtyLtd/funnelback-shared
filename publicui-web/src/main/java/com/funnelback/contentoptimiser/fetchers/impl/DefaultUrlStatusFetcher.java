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
				perlBin);
		runner.addRequestParameter("u", optimiserUrl);
		runner.addRequestParameter("c", collection);		
		runner.addRequestParameter("f", "json");
		runner.setTaint();
		runner.setEnvironmentVariable("REMOTE_USER", "admin");
		String json = "";
		try {
			json = runner.runToString();
		} catch (CgiRunnerException e1) {
			log.error("Unable to run the CGI command to get URL status ",e1);
			return status;
		}
		ObjectMapper mapper = new ObjectMapper();		
/*		Executor getJson = new DefaultExecutor();			
		CommandLine clJson = new CommandLine(new File(searchHome, DefaultValues.FOLDER_WEB + File.separator +  DefaultValues.FOLDER_ADMIN  + File.separator + "url-status.cgi"));
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		getJson.setWorkingDirectory(new File(searchHome, DefaultValues.FOLDER_WEB + File.separator + "admin"));
		ByteArrayOutputStream error = new ByteArrayOutputStream();
		try {
			optimiserUrl = URLEncoder.encode(optimiserUrl,"UTF-8");
			collection = URLEncoder.encode(collection,"UTF-8");
		} catch (UnsupportedEncodingException e1) {
			log.error("UTF-8 is unsupported! This should never happen.",e1);
		}
		clJson.addArgument("?u=" + optimiserUrl + "&c=" + collection + "&f=json");
		Map<String,String> env = null;
		try {
			env = (new DefaultProcessingEnvironment()).getProcEnvironment();
			env.put("REMOTE_USER", "admin");
		} catch (IOException e2) {
			log.error("Unable to obtain the current environment",e2);
		} 
		getJson.setStreamHandler(new PumpStreamHandler(os, error));
		try {
			getJson.execute(clJson,env);
		} catch (ExecuteException e1) {
			log.error("Unable to execute UrlStatus with commandline '" + clJson + "'. Error was: '"+error.toString()+"'",e1);
		} catch (IOException e1) {
			log.error("IOException when executing UrlStatus with commandline " + clJson,e1);
		}*/
		

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

