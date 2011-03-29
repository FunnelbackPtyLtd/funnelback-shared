package com.funnelback.publicui.search.model.collection;

import groovy.lang.Script;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryType;
import com.funnelback.publicui.search.model.collection.facetednavigation.Facet;
import com.funnelback.publicui.search.model.collection.facetednavigation.MetadataBasedType;

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
	@Getter @Setter private Class<Script> transformScriptClass;
	
	/**
	 * @return The list of metadata fields used in this config
	 */
	public List<String> getMetadataFieldsUsed() {
		ArrayList<String> out = new ArrayList<String>();
		if (facets != null) {
			for (Facet facet: facets) {
				if (facet.getCategoryTypes() != null) {
					for (CategoryType ct: facet.getCategoryTypes()) {
						if (ct != null && ct instanceof MetadataBasedType) {
							out.add(((MetadataBasedType) ct).getMetadataClass());
						}
					}
				}
			}
		}
		
		return out;
	}
		
}
