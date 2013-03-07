package com.funnelback.publicui.search.model.collection.paramtransform.operation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import scala.actors.threadpool.Arrays;

/**
 * <p>An {@link Operation} that adds a parameter and its value to the map. The
 * value can possibly be null/blank/empty.</p>
 * 
 * <p>Identical values for a given parameter are merged.</p>
 */
public class AddParameterOperation implements Operation {
    
    private String parameterName;
    private String[] parameterValues;

    public AddParameterOperation(String parameterName, List<String> parameterValues) {
        this.parameterName = parameterName;
        this.parameterValues = parameterValues.toArray(new String[0]);
    }
    
    public AddParameterOperation(String parameterName, String singleValue) {
        this.parameterName = parameterName;
        this.parameterValues = new String[] {singleValue};
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, String[]> apply(final Map<String, String[]> parameters) {
        HashMap<String, String[]> out = new HashMap<String, String[]>(parameters);
        if (out.containsKey(parameterName)) {
            // Only keep unique values, we don't want
            // to have the same value multiple times (FUN-4455)
            Set<String> uniqueValues = new HashSet<String>(Arrays.asList(out.get(parameterName)));
            uniqueValues.addAll(Arrays.asList(parameterValues));
            out.put(parameterName, uniqueValues.toArray(new String[0]));
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
