package com.funnelback.publicui.search.model.collection.paramtransform.operation;

import java.util.Map;

import com.funnelback.publicui.search.model.collection.paramtransform.TransformRule;

/**
 * An operation to apply as part of a {@link TransformRule}
 */
public interface Operation {
	
	/**
	 * Apply the operation on the given parameters
	 * @param parameters
	 */
	public Map<String, String[]> apply(final Map<String, String[]> parameters);
}
