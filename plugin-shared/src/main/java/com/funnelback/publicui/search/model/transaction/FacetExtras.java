package com.funnelback.publicui.search.model.transaction;

import com.funnelback.publicui.search.model.transaction.facet.FacetDisplayType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Holds extra information on faceted navigation.
 * 
 *  @since 15.12
 */
@ToString
@NoArgsConstructor
public class FacetExtras {

    /**
     * The URL required to unselect all selected facets.
     * 
     * @since 15.12
     */
    @Getter @Setter private String unselectAllFacetsUrl;
    
    /**
     * true if {@link SearchResponse#getFacets()} has selected facets
     * which do not have a guessed display type of {@link FacetDisplayType#TAB}
     * 
     * @since 15.12
     */
    @Getter @Setter private boolean hasSelectedNonTabFacets = false;
    
    /**
     * true if {@link SearchResponse#getFacets()} has facets
     * which do not have a guessed display type of {@link FacetDisplayType#TAB}
     * 
     * @since 15.12
     */
    @Getter @Setter private boolean hasNonTabFacets = false;
}
