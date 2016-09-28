package com.funnelback.publicui.search.model.collection;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.MetadataBasedCategory;

/**
 * <p>Faceted navigation configuration.</p>
 * 
 * @see <tt>faceted_navigation.cfg</tt>
 * @since 11.0
 */
@AllArgsConstructor
public class FacetedNavigationConfig {
    
    /** List of facets definitions. */
    @Getter private final List<FacetDefinition> facetDefinitions;
    
    /**
     * Get the list of metadata fields used in this configuration.
     * @return The list of metadata fields used in this configuration.
     */
    public List<String> getMetadataFieldsUsed() {
        ArrayList<String> out = new ArrayList<String>();
        if (facetDefinitions != null) {
            for (FacetDefinition facet: facetDefinitions) {
                if (facet.getCategoryDefinitions() != null) {
                    for (CategoryDefinition cd: facet.getCategoryDefinitions()) {
                        out.addAll(collectMetadataFields(cd));
                    }
                }
            }
        }
        return out;
    }
    
    /**
     * Recursively collect the metadata classes used for a specific {@link CategoryDefinition}.
     * @param definition The {@link CategoryDefinition} to search on.
     * @return The list of metadata classes (Single letters).
     */
    private static List<String> collectMetadataFields(CategoryDefinition definition) {
        ArrayList<String> out = new ArrayList<String>();
        
        if (definition instanceof MetadataBasedCategory) {
            out.add( ((MetadataBasedCategory)definition).getMetadataClass());
        }
        if (definition.getSubCategories() != null) {
            for (CategoryDefinition subDefinition: definition.getSubCategories()) {
                out.addAll(collectMetadataFields(subDefinition));
            }
        }
        return out;
    }
    
    /**
     * @param facetName Facet name to lookup the definition for
     * @return The {@link FacetDefinition} for this facet name, or null if not found
     */
    public FacetDefinition getFacetDefinition(String facetName) {
        for (FacetDefinition fDef: facetDefinitions) {
            if (fDef.getName().equals(facetName)) {
                return fDef;
            }
        }
        
        return null;
    }
        
}
