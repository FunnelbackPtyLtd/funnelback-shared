package com.funnelback.publicui.search.lifecycle.input.processors;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.apachecommons.Log;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.input.InputProcessor;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.utils.MapUtils;

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
	public void processInput(SearchTransaction searchTransaction) {
		if (SearchTransactionUtils.hasQuestion(searchTransaction)) {
			Map<String, String[]> params = searchTransaction.getQuestion().getInputParameterMap();
			HashMap<String, String> out = new HashMap<String, String>();
			
			setIfNotNull(out, Keys.REMOTE_ADDR.toString(), MapUtils.getString(params, Keys.REMOTE_ADDR.toString(), null));
			setIfNotNull(out, Keys.REQUEST_URI.toString(), MapUtils.getString(params, Keys.REQUEST_URI.toString(), null));
			setIfNotNull(out, Keys.AUTH_TYPE.toString(), MapUtils.getString(params, Keys.AUTH_TYPE.toString(), null));
			setIfNotNull(out, Keys.HTTP_HOST.toString(), MapUtils.getString(params, Keys.HTTP_HOST.toString(), null));
			setIfNotNull(out, Keys.REMOTE_USER.toString(), MapUtils.getString(params, Keys.REMOTE_USER.toString(), null));
			
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
