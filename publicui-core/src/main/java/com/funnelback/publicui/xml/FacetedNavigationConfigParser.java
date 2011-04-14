package com.funnelback.publicui.xml;

import java.util.ArrayList;
import java.util.List;

import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;

/**
 * Parses an XML <tt>faceted_navigation.cfg</tt> config file.

 */
public interface FacetedNavigationConfigParser {

	public Facets parseFacetedNavigationConfiguration(String configuration) throws XmlParsingException;
	
	/**
	 * Represent a parsed faceted navigation config.
	 **/
	public class Facets {
		public static final String FACETS = "Facets";
		
		public List<FacetDefinition> facetDefinitions = new ArrayList<FacetDefinition>();
		public String qpOptions;
	}
	
}
