package com.funnelback.publicui.search.lifecycle.input.processors;

import java.util.HashMap;

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
	
	@Override
	public void process(SearchTransaction searchTransaction, HttpServletRequest request) {
		
		HashMap<String, String> out = new HashMap<String, String>();
		
		out.put("REMOTE_ADDR", request.getRemoteAddr());
		out.put("REQUEST_URI", request.getRequestURI());
		if (request.getAuthType() != null) {
			out.put("AUTH_TYPE", request.getAuthType());
		}
		if (request.getHeader("host") != null) {
			out.put("HTTP_HOST", request.getHeader("host"));
		}
		if (request.getRemoteUser() != null) {
			out.put("REMOTE_USER", request.getRemoteUser());
		}
		
		log.debug("Adding environment variables: " + out);
		searchTransaction.getQuestion().getEnvironmentVariables().putAll(out);
		
	}
}
