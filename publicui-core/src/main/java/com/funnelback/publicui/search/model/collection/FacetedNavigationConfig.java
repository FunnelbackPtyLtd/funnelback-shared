package com.funnelback.publicui.search.model.collection;

import groovy.lang.Script;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.MetadataBasedCategory;

/**
 * Faceted navigation configuration.
 * @see <tt>faceted_navigation.cfg</tt>
 */
@AllArgsConstructor
public class FacetedNavigationConfig {
	
	/** Query processor options embedded in the config file */
	@Getter private final String qpOptions;
	
	/** List of facets definitions */
	@Getter private final List<FacetDefinition> facetDefinitions;
	
	/**
	 * Groovy transform script
	 */
	@Getter @Setter private Class<Script> transformScriptClass;
	
	/**
	 * @return The list of metadata fields used in this config
	 */
	public List<String> getMetadataFieldsUsed() {
		ArrayList<String> out = new ArrayList<String>();
		if (facetDefinitions != null) {
			for (FacetDefinition facet: facetDefinitions) {
				if (facet.getCategoryDefinitions() != null) {
					for (CategoryDefinition ct: facet.getCategoryDefinitions()) {
						if (ct != null && ct instanceof MetadataBasedCategory) {
							out.add(((MetadataBasedCategory) ct).getMetadataClass());
						}
					}
				}
			}
		}
		return out;
	}
		
}
