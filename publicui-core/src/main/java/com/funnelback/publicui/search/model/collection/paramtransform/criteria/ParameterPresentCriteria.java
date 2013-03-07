package com.funnelback.publicui.search.model.collection.paramtransform.criteria;

import java.util.Map;

/**
 * A {@link Criteria} that checks that a parameter is present,
 * regardless of its value.
 */
public class ParameterPresentCriteria implements Criteria {
    
    private String parameterName;

    public ParameterPresentCriteria(String parameterName) {
        this.parameterName = parameterName;
    }

    @Override
    public boolean matches(final Map<String, String[]> parameters) {
        return parameters.containsKey(parameterName);
    }

    @Override
    public String toString() {
        return "Parameter '" + parameterName + "' is present";
    }
}
