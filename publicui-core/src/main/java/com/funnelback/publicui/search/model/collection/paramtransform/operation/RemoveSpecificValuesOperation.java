package com.funnelback.publicui.search.model.collection.paramtransform.operation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * An {@link Operation} that removes specific values of a parameter from the
 * map.
 */
public class RemoveSpecificValuesOperation implements Operation {

    private String parameterName;
    private List<String> parameterValues;

    public RemoveSpecificValuesOperation(String parameterName, List<String> parameterValues) {
        this.parameterName = parameterName;
        this.parameterValues = parameterValues;
    }

    @Override
    public Map<String, String[]> apply(Map<String, String[]> parameters) {
        HashMap<String, String[]> out = new HashMap<String, String[]>(parameters);
        if (out.containsKey(parameterName)) {
            String values[] = out.get(parameterName);
            ArrayList<String> newValues = new ArrayList<String>();
            for (String value : values) {
                if (!parameterValues.contains(value)) {
                    newValues.add(value);
                }
            }
            out.put(parameterName, newValues.toArray(new String[0]));
        }
        return out;
    }

    @Override
    public String toString() {
        return "Remove following values of '" + parameterName + "' : " + StringUtils.join(parameterValues, ",");
    }
}
