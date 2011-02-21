package com.funnelback.publicui.search.model.collection.paramtransform.operation;

import java.util.HashMap;
import java.util.Map;

/**
 * An {@link Operation} that removes all the value of a given parameter name
 * from the map.
 * 
 */
public class RemoveAllValuesOperation implements Operation {
	
	private String parameterName;

	public RemoveAllValuesOperation(String parameterName) {
		this.parameterName = parameterName;
	}

	@Override
	public Map<String, String[]> apply(Map<String, String[]> parameters) {
		HashMap<String, String[]> out = new HashMap<String, String[]>(parameters);
		out.remove(parameterName);
		return out;
	}

	@Override
	public String toString() {
		return "Remove all values of '" + parameterName + "'";
	}
}
