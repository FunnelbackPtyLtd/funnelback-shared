package com.funnelback.publicui.search.model.padre;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Contextual navigation navigation details
 */
@AllArgsConstructor
public class ClusterNav {

	@Getter @Setter private Integer level;
	@Getter @Setter private String url;
	@Getter @Setter private String label;
	
	public final static class Schema {
		public static final String CLUSTER_NAV = "cluster_nav";
		
		public static final String LEVEL = "level";
		public static final String URL = "url";
	}
}
