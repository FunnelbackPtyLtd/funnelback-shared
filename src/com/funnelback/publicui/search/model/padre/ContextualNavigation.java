package com.funnelback.publicui.search.model.padre;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Contextual navigation
 */
@NoArgsConstructor
public class ContextualNavigation {

	@Getter @Setter private String searchTerm;
	@Getter @Setter private ClusterNav clusterNav;
	
	@Getter private final List<Category> categories = new ArrayList<Category>();
	
	public final static class Schema {
		public static final String CONTEXTUAL_NAVIGATION = "contextual_navigation";
		
		public static final String SEARCH_TERMS = "search_terms";
	}
	
}
