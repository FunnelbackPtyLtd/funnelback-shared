package com.funnelback.publicui.web.filters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang.ArrayUtils;

import com.funnelback.publicui.search.model.collection.ParameterTransformation;
import com.funnelback.publicui.web.utils.QueryStringUtils;

/**
 * Wraps an {@link HttpServletRequest} in order to allow parameter manipulation.
 * 
 * Since {@link HttpServletRequest} objects are immutable, this is the recommended way
 * to manipulate query string parameters.
 * 
 * This wrapper is used in conjunction with {@link RequestParametersTransformFilter} to
 * achieve CGI Transforms.
 * 
 * @see HttpServletRequestWrapper *
 */
public class RequestParametersTransformWrapper extends HttpServletRequestWrapper {

	/**
	 * Tranform rule syntax is:
	 * replaced_param=value => insert_param1=value&insert_param2=value
	 * param=value => -remove_param1
	 * ...
	 * 
	 * @see Original Perl code
	 */
	private static final Pattern RULE_PATTERN = Pattern.compile("^\\s*([^\\s]+?)\\s*=>\\s*([^\\s]+?)\\s*$");
	private static final Pattern FROM_PATTERN = Pattern.compile("^\\s*([^=]+)(\\s*=\\s*(.*))?\\s*");
	private static final Pattern TO_PATTERN = Pattern.compile("^\\s*(\\-)?(.+)?\\s*");
	
	/** Modified parameters Map */
	private HashMap<String, String[]> modifiedParameterMap = new HashMap<String, String[]>();
	
	public RequestParametersTransformWrapper(HttpServletRequest request, String[] rules) {
		super(request);
		modifiedParameterMap.putAll(request.getParameterMap());
		
		ParameterTransformation pt = new ParameterTransformation();
		pt.initRules(rules);
		pt.apply(modifiedParameterMap);
	}
	
	@SuppressWarnings("unchecked")
	public void  __OLD__RequestParametersTransformWrapper(HttpServletRequest request, String[] rules) {
		// super(request);
		modifiedParameterMap.putAll(request.getParameterMap());
		
		if (rules != null) {
			for(String rule: rules) {
				Matcher m = RULE_PATTERN.matcher(rule);
				if (m.find()) {
					String from = m.group(1);
					String to = m.group(2);
					
					Matcher fromMatcher = FROM_PATTERN.matcher(from);
					if (fromMatcher.matches()) {
						String paramName = fromMatcher.group(1);
	
						// Does a parameter with this name exists ?
						String[] values = (String[]) request.getParameterMap().get(paramName);
						if (values != null) {
							
							if (fromMatcher.group(3) != null && ! "".equals(fromMatcher.group(3))) {
								// Test if a specific value exists
								String paramValue = fromMatcher.group(3);
								if (! ArrayUtils.contains(values, paramValue)) {
									continue;
								}
							}
	
							Matcher toMatcher = TO_PATTERN.matcher(to);
							if (toMatcher.find()) {
								String pv = toMatcher.group(2);
								
								if (toMatcher.group(1) == null) {
									// Do not remove
									Map<String, List<String>> params = QueryStringUtils.toMap(pv, false);
									for (String name: params.keySet()) {
										modifiedParameterMap.put(name, params.get(name).toArray(new String[0]));
									}								
								} else {
									// Remove
									String[] params = pv.split("&");
									for (String param: params) {
										String[] kv = param.split("=");
										if (kv.length<2 || kv[1] == null) {
											// Remove all
											modifiedParameterMap.remove(kv[0]);
										} else {
											// Remove only matching values
											ArrayList<String> newValues = new ArrayList<String>();
											newValues.addAll(Arrays.asList(modifiedParameterMap.get(kv[0])));
											newValues.remove(kv[1]);
											modifiedParameterMap.put(kv[0], newValues.toArray(new String[0]));
	
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	@Override
	public Map getParameterMap() {
		return modifiedParameterMap;
	}

	@Override
	public String getParameter(String name) {
		String[] values = modifiedParameterMap.get(name);
		if(values != null && values.length > 0) {
			return values[0];
		} else {
			return null;
		}
	}
	
	@Override
	public String[] getParameterValues(String name) {
		return modifiedParameterMap.get(name);
	}
	
	@Override
	public Enumeration<String> getParameterNames() {
		return Collections.enumeration(modifiedParameterMap.keySet());
	}
	
}
