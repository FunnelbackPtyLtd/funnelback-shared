package com.funnelback.publicui.search.model.collection.facetednavigation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.constraints.NotNull;

import com.funnelback.common.facetednavigation.models.Facet;
import com.funnelback.common.facetednavigation.models.FacetConstraintJoin;
import com.funnelback.common.facetednavigation.models.FacetSelectionType;
import com.funnelback.common.facetednavigation.models.FacetValues;
import com.funnelback.common.facetednavigation.models.FacetValuesOrder;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * <p>Definition of a facet.</p>
 * 
 * <p>Has a name, and a list of category definitions.</p>
 * 
 * @since 11.0
 */
@ToString
@RequiredArgsConstructor
public class FacetDefinition {

    /** Name of the facet (Ex: "Location") */
    @Getter private final String name;
    
    /**
     * <p>List of category definitions.</p>
     * 
     * <p>A category definition defines how the values for this
     * facet will be calculated, based on metadata counts, URL counts
     * or GScope counts.</p>
     */
    @Getter private final List<CategoryDefinition> categoryDefinitions;
    
    @NotNull @NonNull
    @Getter private FacetSelectionType selectionType;
    
    @NotNull @NonNull
    @Getter private FacetConstraintJoin constraintJoin;
    
    @NotNull @NonNull
    @Getter private FacetValues facetValues;
    
    @NotNull @NonNull
    @Getter private List<FacetValuesOrder> order;
    
    /**
     * Recursively get the list of all query string parameters used.
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
    
    /** Constants for the <code>facete_navigation.cfg</code> XML tags. */
    public final class Schema {
        public static final String FACET = "Facet";
        public static final String DATA = "Data";
    }
    
    public static FacetDefinition getFacetWithUpgradedValues(
            String name, 
            List<CategoryDefinition> categoryDefinitions) {
        return new FacetDefinition(name, 
            categoryDefinitions, 
            Facet.LEGACY_FACET_SELECTION_TYPE, 
            Facet.LEGACY_FACET_CONSTRAINT_JOIN, 
            Facet.LEGACY_FACET_VALUES,
            Facet.LEGACY_FACET_ORDER);
    }
}
