package com.funnelback.publicui.search.model.collection.paramtransform.criteria;

import java.util.Map;

import com.funnelback.publicui.search.model.collection.paramtransform.TransformRule;

/**
 * A criteria to match, part of a {@link TransformRule}.
 */
public interface Criteria {
    
    /**
     * @param parameters
     * @return true if the criteria matches the input parameter map.
     */
    public boolean matches(final Map<String, String[]> parameters);
}

