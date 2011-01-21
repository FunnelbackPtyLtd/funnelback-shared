package com.funnelback.publicui.search.model.padre;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Contextual navigation cluster (belongs to a {@link Category})
 */
@RequiredArgsConstructor
public class Cluster {

	@Getter private final String href;
	@Getter private final Integer count;
	@Getter private final String label;
	
	
	
	public final static class Schema {
		public static final String CLUSTER = "cluster";
		
		public static final String HREF = "href";
		public static final String COUNT = "count";
	}
}
