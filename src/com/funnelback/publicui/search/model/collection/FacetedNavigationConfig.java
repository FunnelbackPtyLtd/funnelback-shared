package com.funnelback.publicui.search.model.collection;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.funnelback.publicui.search.model.collection.facetednavigation.Facet;

/**
 * Faceted navigation configuration.
 * @see faceted_navigation.cfg
 */
@RequiredArgsConstructor
public class FacetedNavigationConfig {
	
	/** Query processor options embedded in the config file */
	@Getter private final String qpOptions;
	
	/** List of facets */
	@Getter private final List<Facet> facets;
	
}
