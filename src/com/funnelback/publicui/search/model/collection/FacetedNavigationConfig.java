package com.funnelback.publicui.search.model.collection;

import groovy.lang.Script;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import com.funnelback.publicui.search.model.collection.facetednavigation.Facet;

/**
 * Faceted navigation configuration.
 * @see faceted_navigation.cfg
 */
@AllArgsConstructor
public class FacetedNavigationConfig {
	
	/** Query processor options embedded in the config file */
	@Getter private final String qpOptions;
	
	/** List of facets */
	@Getter private final List<Facet> facets;
	
	/**
	 * Groovy transform script
	 */
	@Getter @Setter private Script transformScript;
		
}
