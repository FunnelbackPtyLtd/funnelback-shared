package com.funnelback.publicui.search.model.collection.paramtransform.operation;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

public class AddParameterOperation implements Operation {
	private String parameterName;
	private String[] parameterValues;
	public AddParameterOperation(String parameterName, List<String> parameterValues) {
		this.parameterName = parameterName;
		this.parameterValues = parameterValues.toArray(new String[0]);
	}
	
	@Override
	public void apply(Map<String, String[]> parameters) {
		if (parameters.containsKey(parameterName)) {
			String values[] = parameters.get(parameterName);
			parameters.put(parameterName, (String[]) ArrayUtils.addAll(values, parameterValues));
		} else {
			parameters.put(parameterName, parameterValues);
		}
	}
	
	@Override
	public String toString() {
		return "Add parameter '" + parameterName +"' = '" + StringUtils.join(parameterValues, ",") + "'";
	}
	
}
