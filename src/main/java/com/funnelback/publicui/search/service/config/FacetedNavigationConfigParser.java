package com.funnelback.publicui.search.service.config;

import java.util.ArrayList;
import java.util.List;

import com.funnelback.publicui.search.model.collection.facetednavigation.Facet;
import com.funnelback.publicui.xml.XmlParsingException;

public interface FacetedNavigationConfigParser {

	public Facets parseFacetedNavigationConfiguration(String configuration) throws XmlParsingException;
	
	public class Facets {
		public static final String FACETS = "Facets";
		
		public List<Facet> facets = new ArrayList<Facet>();
		public String qpOptions;
	}
	
}
