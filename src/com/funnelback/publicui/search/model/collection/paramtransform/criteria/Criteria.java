package com.funnelback.publicui.search.model.collection.paramtransform.criteria;

import java.util.Map;

public interface Criteria {
	public boolean matches(final Map<String, String[]> parameters);
}

