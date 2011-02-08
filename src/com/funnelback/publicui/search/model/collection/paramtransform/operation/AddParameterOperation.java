package com.funnelback.publicui.search.model.collection.paramtransform.operation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

/**
 * An {@link Operation} that adds a parameter and its value to the map. The
 * value can possibly be null/blank/empty.
 */
public class AddParameterOperation implements Operation {
	
	private String parameterName;
	private String[] parameterValues;

	public AddParameterOperation(String parameterName, List<String> parameterValues) {
		this.parameterName = parameterName;
		this.parameterValues = parameterValues.toArray(new String[0]);
	}

	@Override
	public Map<String, String[]> apply(final Map<String, String[]> parameters) {
		HashMap<String, String[]> out = new HashMap<String, String[]>(parameters);
		if (out.containsKey(parameterName)) {
			String values[] = out.get(parameterName);
			out.put(parameterName, (String[]) ArrayUtils.addAll(values, parameterValues));
		} else {
			out.put(parameterName, parameterValues);
		}
		return out;
	}

	@Override
	public String toString() {
		return "Add parameter '" + parameterName + "' = '" + StringUtils.join(parameterValues, ",") + "'";
	}

}
