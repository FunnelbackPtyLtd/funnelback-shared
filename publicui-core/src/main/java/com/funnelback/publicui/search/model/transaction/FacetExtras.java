package com.funnelback.publicui.search.model.transaction;

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
}
