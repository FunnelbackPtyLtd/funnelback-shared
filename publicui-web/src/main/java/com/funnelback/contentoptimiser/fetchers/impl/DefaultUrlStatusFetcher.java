package com.funnelback.contentoptimiser.fetchers.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import lombok.extern.apachecommons.Log;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.environment.DefaultProcessingEnvironment;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.contentoptimiser.UrlStatus;
import com.funnelback.contentoptimiser.fetchers.UrlStatusFetcher;

@Log
@Component
public class DefaultUrlStatusFetcher implements UrlStatusFetcher {

	@Autowired
	File searchHome;
	

	@SuppressWarnings("unchecked")
	@Override
	public UrlStatus fetch(String optimiserUrl, String collection) {
		String json = "{\"error\":\"Never seen\",\"message\":\"<p>No information available for URL: <a href=\\\"http://www.apsjobs.gov.au/\\\">http://www.apsjobs.gov.au/</a></p><p>http://www.apsjobs.gov.au/ passed the web crawler loading policy.</p>\",\"available\":\"false\"}";
		

		Executor getJson = new DefaultExecutor();			
		CommandLine clJson = new CommandLine(new File(searchHome, DefaultValues.FOLDER_WEB + File.separator + "admin"/* DefaultValues.FOLDER_ADMIN */ + File.separator + "url-status.cgi"));
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
		}
		
		ObjectMapper mapper = new ObjectMapper();
		UrlStatus status = null;
		try {
			String string = os.toString();
			// strip header
			string = string.replaceFirst("(?s).*Content-Type: application/json", "");
			status = mapper.readValue(string, UrlStatus.class);
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
