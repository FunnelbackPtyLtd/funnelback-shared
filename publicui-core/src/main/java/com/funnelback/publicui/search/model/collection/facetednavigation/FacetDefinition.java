package com.funnelback.publicui.search.model.collection.facetednavigation;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Definition of a facet.
 * 
 * Has a name, and a list of category definitions.
 *
 */
@ToString
@RequiredArgsConstructor
public class FacetDefinition {

	@Getter private final String name;
	@Getter private final List<CategoryDefinition> categoryDefinitions;
	
	public final class Schema {
		public static final String FACET = "Facet";
		public static final String DATA = "Data";
	}
}
