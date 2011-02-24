package com.funnelback.publicui.web.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

/**
 * Filters a list of {@link HttpServletRequest} parameters
 * depending of a regex.
 */
public class RequestParametersFilter {

	private static final String[] CONVERSION_ARRAY = new String[0];
	
	private String[] parameterNames;
	
	@SuppressWarnings("unchecked")
	public RequestParametersFilter(HttpServletRequest request) {
		parameterNames = (String[]) request.getParameterMap().keySet().toArray(CONVERSION_ARRAY); 
	}
	
	public String[] filter(Pattern p) {
		List<String> out = new ArrayList<String>();
		for(String name: parameterNames) {
			if (p.matcher(name).matches()) {
				out.add(name);
			}
		}
		return out.toArray(CONVERSION_ARRAY);
	}
	
	public String[] filter(String regex){
		return filter(Pattern.compile(regex));
	}
	
}
