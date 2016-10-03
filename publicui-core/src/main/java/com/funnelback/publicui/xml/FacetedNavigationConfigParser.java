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

    public Facets parseFacetedNavigationConfiguration(byte[] configuration) throws FacetedNavigationConfigParseException;
    
    /**
     * Represent a parsed faceted navigation config.
     **/
    public class Facets {
        public static final String FACETS = "Facets";
        
        public List<FacetDefinition> facetDefinitions = new ArrayList<FacetDefinition>();
    }
    
    
    public static class FacetedNavigationConfigParseException extends Exception {
        
        public FacetedNavigationConfigParseException(String message) {
            super(message);
        }
        
        public FacetedNavigationConfigParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
}
