package com.funnelback.publicui.search.model.collection.paramtransform.criteria;

import java.util.Map;

import org.apache.commons.lang.ArrayUtils;

public class ParameterMatchesValueCriteria implements Criteria {
	private String parameterName;
	private String parameterValue;

	public ParameterMatchesValueCriteria(String parameterName, String parameterValue) {
		this.parameterName = parameterName;
		this.parameterValue = parameterValue;
	}

	@Override
	public boolean matches(final Map<String, String[]> parameters) {
		if (parameters.containsKey(parameterName)) {
			String[] values = parameters.get(parameterName);
			return ArrayUtils.contains(values, parameterValue);
		}
		return false;
	}

	@Override
	public String toString() {
		return "Parameter '" + parameterName + "' is present and has value '" + parameterValue + "'";
	}
}
