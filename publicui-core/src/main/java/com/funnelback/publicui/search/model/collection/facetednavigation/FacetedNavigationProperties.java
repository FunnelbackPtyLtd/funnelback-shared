package com.funnelback.publicui.search.model.collection.facetednavigation;

import com.funnelback.common.facetednavigation.models.FacetConstraintJoin;
import com.funnelback.common.facetednavigation.models.FacetSelectionType;

public class FacetedNavigationProperties {
    
    /**
     * Is it possible that selecting something in this facet will expand the result set?
     * 
     * For OR facets when we select a value we can increase the result set as we go from just
     * blue cars to red or blue vars.
     * 
     * For SINGLE_AND_UNSELECT_OTHER_FACETS we can unselect some checked facets which means the result
     * set can be exapnded upon selection.
     *  
     * @param facet
     * @return
     */
    public boolean canSelectingTheFacetExpandTheResultSet(FacetDefinition facet) {
        return facet.getConstraintJoin() == FacetConstraintJoin.OR 
            || facet.getSelectionType()== FacetSelectionType.SINGLE_AND_UNSELECT_OTHER_FACETS;
    }
}
