package com.funnelback.publicui.search.model.collection.paramtransform.operation;

import java.util.Map;

public class RemoveAllValuesOperation implements Operation {
	private String parameterName;

	public RemoveAllValuesOperation(String parameterName) {
		this.parameterName = parameterName;
	}

	@Override
	public void apply(Map<String, String[]> parameters) {
		parameters.remove(parameterName);
	}

	@Override
	public String toString() {
		return "Remove all values of '" + parameterName + "'";
	}
}
