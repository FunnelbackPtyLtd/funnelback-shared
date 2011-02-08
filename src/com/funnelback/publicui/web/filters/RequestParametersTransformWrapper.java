package com.funnelback.publicui.web.filters;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.funnelback.publicui.search.model.collection.paramtransform.TransformRule;
import com.funnelback.publicui.search.model.collection.paramtransform.operation.Operation;

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
	
	/** Modified parameters Map */
	private Map<String, String[]> modifiedParameterMap = new HashMap<String, String[]>();
	
	public RequestParametersTransformWrapper(HttpServletRequest request, List<TransformRule> rules) {
		super(request);
		modifiedParameterMap.putAll(request.getParameterMap());
		
		if (rules != null) {
			for (TransformRule rule: rules) {
				if (rule.getCriteria().matches(request.getParameterMap())) {
					for (Operation o: rule.getOperations()) {
						modifiedParameterMap = o.apply(Collections.unmodifiableMap(modifiedParameterMap));
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
