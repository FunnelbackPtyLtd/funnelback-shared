package com.funnelback.publicui.search.model.collection.facetednavigation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	
	/**
	 * @return The list of every query string parameter used in this
	 * facet definition, for every categories definition and sub category definitions.
	 */
	public Set<String> getAllQueryStringParamNames() {
		HashSet<String> out = new HashSet<String>();
		if (categoryDefinitions != null) {
			for (CategoryDefinition definition: categoryDefinitions) {
				out.addAll(definition.getAllQueryStringParamNames());
			}
		}
		return out;
	}
	
	public final class Schema {
		public static final String FACET = "Facet";
		public static final String DATA = "Data";
	}
}
