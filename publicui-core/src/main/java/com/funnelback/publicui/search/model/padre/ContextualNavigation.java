package com.funnelback.publicui.search.model.padre;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Contextual navigation parent class.
 * 
 * @since 11.0
 * @see Category
 * @see Cluster
 * @see ClusterNav
 */
@NoArgsConstructor
public class ContextualNavigation {

	/** Terms used within this contextual navigation object */
	@Getter @Setter private String searchTerm;
	
	/** Navigation items */
	@Getter @Setter private ClusterNav clusterNav;
	
	/** List of categories (type, topic, site). */
	@Getter private final List<Category> categories = new ArrayList<Category>();
	
	/**
	 * Custom data placeholder allowing any arbitrary data to be
	 * stored by hook scripts.
	 */
	@Getter private final Map<String, Object> customData = new HashMap<String, Object>();

	/** Constants for the PADRE XML result packet tags. */
	public final static class Schema {
		public static final String CONTEXTUAL_NAVIGATION = "contextual_navigation";
		
		public static final String SEARCH_TERMS = "search_terms";
	}
	
}
