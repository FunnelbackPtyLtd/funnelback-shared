package com.funnelback.publicui.search.lifecycle.input.processors;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.log4j.Log4j;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.input.InputProcessor;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;

/**
 * Will collect some environment variables from the request that will
 * be repassed as environment variables to PADRE.
 */
@Component("passThroughEnvironmentVariabesInputProcessor")
@Log4j
public class PassThroughEnvironmentVariables implements InputProcessor {

	// FIXME Found these other ones in PADRE source code. Are they really needed ?
	// SCRIPT_NAME, SERVER_SOFTWARE: Apparently used when PADRE outputs directly HTML
	// SITE_SEARCH_ROOT: Used for Matrix OEM
	public enum Keys {
		REMOTE_ADDR, REQUEST_URI, AUTH_TYPE, HTTP_HOST, REMOTE_USER, HTTP_REFERER, X_FORWARDED_FOR;
	}


	
	@Override
	public void processInput(SearchTransaction searchTransaction) {
		if (SearchTransactionUtils.hasQuestion(searchTransaction)) {
			Map<String, String> params = searchTransaction.getQuestion().getInputParameterMap();
			HashMap<String, String> out = new HashMap<String, String>();
			
			for (Keys key: Keys.values()) {
				setIfNotNull(out, key.toString(), params.get(key.toString()));
			}
			
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
