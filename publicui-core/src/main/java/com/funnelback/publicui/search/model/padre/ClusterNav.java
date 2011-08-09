package com.funnelback.publicui.search.model.padre;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>Contextual navigation details to navigate through
 * results and clusters.</p>
 * 
 * <p>
 * For example you can follow a navigation path like:
 * <ul>
 * 	<li>King</li>
 *  <li>King Richard</li>
 *  <li>King Richard the great</li>
 * </ul>
 * 
 * For each step a {@link ClusterNav} item will be provided.
 * </p>
 * 
 * @since 11.0
 * @see {@link ContextualNavigation}
 * @see {@link Category}
 * @see {@link Cluster} 
 */
@AllArgsConstructor
public class ClusterNav {

	/**
	 * <p>Level of this navigation item.</p>
	 * <p>0 is used for the root, then 1, 2, etc.</p>
	 */
	@Getter @Setter private Integer level;
	
	/**
	 * URL to reach this navigation item.</p>
	 */
	@Getter @Setter private String url;
	
	/**
	 * Label for this navigation item, such as "King Richard".
	 */
	@Getter @Setter private String label;
	
	/** Constants for the PADRE XML result packet tags. */
	public final static class Schema {
		public static final String CLUSTER_NAV = "cluster_nav";
		
		public static final String LEVEL = "level";
		public static final String URL = "url";
	}
}
