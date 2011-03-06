package com.funnelback.publicui.search.lifecycle.input.processors;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.apachecommons.Log;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.input.InputProcessor;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * Will collect some environment variables from the request that will
 * be repassed as environment variables to PADRE.
 */
@Component("passThroughEnvironmentVariabesInputProcessor")
@Log
public class PassThroughEnvironmentVariables implements InputProcessor {

	// FIXME Found these other ones in PADRE source code. Are they really needed ?
	// SCRIPT_NAME, SERVER_SOFTWARE: Apparently used when PADRE outputs directly HTML
	// SITE_SEARCH_ROOT: Used for Matrix OEM
	
	public enum Keys {
		REMOTE_ADDR, REQUEST_URI, AUTH_TYPE, HTTP_HOST, REMOTE_USER;
	}
	
	@Override
	public void process(SearchTransaction searchTransaction, HttpServletRequest request) {
		if (searchTransaction != null
				&& searchTransaction.getQuestion() != null
				&& request != null) {
			HashMap<String, String> out = new HashMap<String, String>();
			
			setIfNotNull(out, Keys.REMOTE_ADDR.toString(), request.getRemoteAddr());
			setIfNotNull(out, Keys.REQUEST_URI.toString(), request.getRequestURI());
			setIfNotNull(out, Keys.AUTH_TYPE.toString(), request.getAuthType());
			setIfNotNull(out, Keys.HTTP_HOST.toString(), request.getHeader("host"));
			setIfNotNull(out, Keys.REMOTE_USER.toString(), request.getRemoteUser());
			
			log.debug("Adding environment variables: " + out);
			searchTransaction.getQuestion().getEnvironmentVariables().putAll(out);
		}		
	}
	
	private void setIfNotNull(Map<String, String> out, String key, String data) {
		if (data != null) {
			out.put(key, data);
		}
	}
}
