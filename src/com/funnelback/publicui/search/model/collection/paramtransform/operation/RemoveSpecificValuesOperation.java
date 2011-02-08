package com.funnelback.publicui.search.model.collection.paramtransform.operation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class RemoveSpecificValuesOperation implements Operation {
	private String parameterName;
	private List<String> parameterValues;

	public RemoveSpecificValuesOperation(String parameterName, List<String> parameterValues) {
		this.parameterName = parameterName;
		this.parameterValues = parameterValues;
	}

	@Override
	public void apply(Map<String, String[]> parameters) {
		if (parameters.containsKey(parameterName)) {
			String values[] = parameters.get(parameterName);
			ArrayList<String> newValues = new ArrayList<String>();
			for (String value : values) {
				if (!parameterValues.contains(value)) {
					newValues.add(value);
				}
			}
			parameters.put(parameterName, newValues.toArray(new String[0]));
		}
	}

	@Override
	public String toString() {
		return "Remove following values of '" + parameterName + "' : " + StringUtils.join(parameterValues, ",");
	}
}
