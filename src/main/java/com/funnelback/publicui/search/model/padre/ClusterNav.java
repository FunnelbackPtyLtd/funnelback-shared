package com.funnelback.publicui.search.model.padre;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Contextual navigation navigation details
 */
@RequiredArgsConstructor
public class ClusterNav {

	@Getter private final  Integer level;
	@Getter private final String url;
	@Getter private final String label;
	
	public final static class Schema {
		public static final String CLUSTER_NAV = "cluster_nav";
		
		public static final String LEVEL = "level";
		public static final String URL = "url";
	}
}
