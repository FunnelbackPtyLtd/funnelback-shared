package com.funnelback.publicui.search.model.transaction.facet;

import com.funnelback.common.facetednavigation.models.FacetConstraintJoin;
import com.funnelback.common.facetednavigation.models.FacetSelectionType;
import com.funnelback.common.facetednavigation.models.FacetValues;

public enum FacetDisplayType {
    SINGLE_DRILL_DOWN, // Don't make a distinction between SINGLE and DRILL down as URL 
                       // Fill would look like it sits under SINGLE but actually be under 
                       // DRILL down.
    CHECKBOX_AND,
    CHECKBOX_OR,
    RADIO_BUTTON,
    TAB,
    UNKNOWN;
    
    public static FacetDisplayType getType(FacetSelectionType selectionType, 
                                        FacetConstraintJoin constraintJoin, 
                                        FacetValues values) {
        if(selectionType == FacetSelectionType.MULTIPLE) {
            if(constraintJoin == FacetConstraintJoin.AND) {
                return CHECKBOX_AND;
            } else {
                return CHECKBOX_OR;
            }
        }
        if(selectionType == FacetSelectionType.SINGLE_AND_UNSELECT_OTHER_FACETS) {
            return TAB;
        }
        if(selectionType == FacetSelectionType.SINGLE) {
            if(values == FacetValues.FROM_SCOPED_QUERY ||
                values == FacetValues.FROM_SCOPED_QUERY_HIDE_UNSELECTED_PARENT_VALUES) {
                return SINGLE_DRILL_DOWN;
            } else {
                return RADIO_BUTTON;
            }
        }
        
        return UNKNOWN;
    }
}
