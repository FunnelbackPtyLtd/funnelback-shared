package com.funnelback.publicui.search.model.collection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * A search profile, whose configuration is residing in
 * a sub directory in the main collection conf/ directory
 */
@RequiredArgsConstructor
public class Profile {

	/** Profile id */
	@Getter private final String id;
	
	/** Faceted navigation configuration, from the conf/ directory */
	@Getter @Setter private FacetedNavigationConfig facetedNavConfConfig;
	
	/** Faceted navigation configuration, from the live/idx/ directory */
	@Getter @Setter private FacetedNavigationConfig facetedNavLiveConfig;
	
}
