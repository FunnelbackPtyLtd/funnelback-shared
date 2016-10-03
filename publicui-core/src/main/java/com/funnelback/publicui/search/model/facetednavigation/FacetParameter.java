package com.funnelback.publicui.search.model.facetednavigation;

import lombok.Data;

/**
 * Representation of a parameter for selecting a particular facet category (or
 * categories) within the faceted navigation system.
 */
@Data
public class FacetParameter {

    /** The name of the facet to which this selection applies. */
    private final String name;
    
    /** Additional parameter information specific to the selected facet. */
    private final String extraParameter;
    
    /** The category values selected within the facet. */
    private final String[] values;
}
