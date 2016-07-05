package com.funnelback.publicui.xml;

import java.util.ArrayList;
import java.util.List;

import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;

/**
 * Parses an XML <tt>faceted_navigation.cfg</tt> config file.
 * 
 * <p>Actually this can parse faceted nav files which exist in live</p>

 */
public interface FacetedNavigationConfigParser {

    public Facets parseFacetedNavigationConfiguration(byte[] configuration); //TODO we need a checked exception here
    
    /**
     * Represent a parsed faceted navigation config.
     **/
    public class Facets {
        public static final String FACETS = "Facets";
        
        public List<FacetDefinition> facetDefinitions = new ArrayList<FacetDefinition>();
        
        //TODO fill these out.
        public String qpOptions;
    }
    
}
