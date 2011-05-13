package com.funnelback.publicui.utils.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import lombok.Getter;

/**
 * Represents a CGI environment, with variables
 * taken from an {@link HttpServletRequest}
 */
public class CGIEnvironment {

	@Getter private Map<String, String> environment = new HashMap<String, String>();
	
	public CGIEnvironment(HttpServletRequest request) {
		putIfNotNull(environment,"CONTENT_LENGTH", Integer.toString(request.getContentLength()));
		putIfNotNull(environment,"CONTENT_TYPE", request.getContentType());

		putIfNotNull(environment,"PATH_INFO", request.getPathInfo());
		putIfNotNull(environment,"PATH_TRANSLATED", request.getPathTranslated());
		putIfNotNull(environment,"QUERY_STRING", request.getQueryString());
		putIfNotNull(environment,"REQUEST_METHOD", request.getMethod());

		putIfNotNull(environment,"AUTH_TYPE", request.getAuthType());
		putIfNotNull(environment,"REMOTE_ADDR", request.getRemoteAddr());
		putIfNotNull(environment,"REMOTE_HOST", request.getRemoteHost());
		putIfNotNull(environment,"REMOTE_USER", request.getRemoteUser());

		putIfNotNull(environment,"SCRIPT_NAME", request.getServletPath());
		putIfNotNull(environment,"SERVER_NAME", request.getServerName());
		putIfNotNull(environment,"SERVER_PORT", Integer.toString(request.getServerPort()));
		putIfNotNull(environment,"SERVER_PROTOCOL", request.getProtocol());
	}
	
	private void putIfNotNull(Map<String, String> map, String key, String value) {
		if (value != null) {
			map.put(key, value);
		}
	}
	
}
