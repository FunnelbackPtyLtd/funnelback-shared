package com.funnelback.publicui.search.model.collection;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * <p>A search profile.</p>
 * 
 * <p>Profile configurations resides in a sub directory
 * in the main collection's <code>conf/[collection]/</code> directory.<p>
 * 
 * <p>Each profile can have specific query processor options and a specific
 * faceted navigation configuration.</p>
 * 
 * <p>Each collection comes with two default profiles, <code>_default</code> and
 * <code>_default_preview</code> used in the preview / publish system.
 * 
 * @since 11.0
 */
@NoArgsConstructor
public class Profile {

	/**
	 * Profile id, identical to the name of the folder containing the
	 * profile configuration under <code>conf/[collection]/[profile]/</code>.
	 */
	@Getter private String id;
	
	/**
	 * Faceted navigation configuration in
	 * <code>conf/[collection]/[profile]/faceted_navigation.cfg</code>
	 */
	@Getter @Setter private FacetedNavigationConfig facetedNavConfConfig;
	
	/**
	 * Faceted navigation configuration in
	 * <code>data/[collection]/live/idx/[profile]/faceted_navigation.cfg</code>
	 */
	@Getter @Setter private FacetedNavigationConfig facetedNavLiveConfig;
	
	/**
	 * <p>Specific query processor options for this profile.</p>
	 * 
	 * <p>Read from <code>conf/[collection]/[profile]/padre_opts.cfg</code>.</p>
	 */
	@Getter @Setter private String padreOpts;
	
	public Profile(String id) {
		this.id = id;
	}
	
}
