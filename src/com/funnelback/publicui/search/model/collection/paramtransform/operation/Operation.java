package com.funnelback.publicui.search.model.collection.paramtransform.operation;

import java.util.Map;

public interface Operation {
	public void apply(Map<String, String[]> parameters);
}
