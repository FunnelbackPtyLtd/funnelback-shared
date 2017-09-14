package com.funnelback.publicui.search.model.collection.facetednavigation;

import java.util.List;
import java.util.Map;

import com.funnelback.common.facetednavigation.models.FacetConstraintJoin;
import com.funnelback.common.facetednavigation.models.FacetSelectionType;
import com.funnelback.common.facetednavigation.models.FacetValues;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.utils.FacetedNavigationUtils;

public class FacetedNavigationProperties {
    
    /**
     * Do we need to run a dedicated extra search to work out the counts?
     * 
     * For a selected facet that is a OR type facet we do because the result set could be expanded when
     * selected. Thus counting rmcf/gscopes in the main search will come out under and counting in the unscoped
     * extra search will come out over so we need a dedicated extra search.
     * 
     * We don't need to run the extra search if the facet is not selected as clicking on it (as between
     * facets we always AND) the result set will go down so the main sarch counts can be used.
     *  
     * @param facet
     * @return
     */
    public boolean useDedicatedExtraSearchForCounts(FacetDefinition facet, SearchTransaction searchTransaction) {
        Map<String, List<String>> selectedCategoryValues  = searchTransaction.getQuestion().getSelectedCategoryValues();
        
        if(facet.getConstraintJoin() == FacetConstraintJoin.LEGACY) {
            return false;
        }
        
        if(facet.getSelectionType() == FacetSelectionType.MULTIPLE
            && facet.getConstraintJoin() == FacetConstraintJoin.OR
            && FacetedNavigationUtils.isFacetSelected(facet, selectedCategoryValues)) {
            return true;
        }
        
        // We could instead run a query where the given facet is unselected and then use those counts.
        
        
        return false;
    }
    
    public boolean useScopedSearchWithFacetDisabledForCounts(FacetDefinition facet, SearchTransaction searchTransaction) {
        if(facet.getConstraintJoin() == FacetConstraintJoin.LEGACY) {
            return false;
        }
        Map<String, List<String>> selectedCategoryValues  = searchTransaction.getQuestion().getSelectedCategoryValues();
        // AKA Radio.
        if(facet.getSelectionType() == FacetSelectionType.SINGLE 
            && (facet.getFacetValues() == FacetValues.FROM_UNSCOPED_QUERY || facet.getFacetValues() == FacetValues.FROM_UNSCOPED_ALL_QUERY)
            && FacetedNavigationUtils.isFacetSelected(facet, selectedCategoryValues)) {
            // probably something like a radio button.
            return true;
        }
        
        return false;
    }
    
    public boolean useScopedSearchWithFacetDisabledForValues(FacetDefinition facet, SearchTransaction searchTransaction) {
        if(facet.getConstraintJoin() == FacetConstraintJoin.LEGACY) {
            return false;
        }
        Map<String, List<String>> selectedCategoryValues  = searchTransaction.getQuestion().getSelectedCategoryValues();
        if(facet.getFacetValues() == FacetValues.FROM_SCOPED_QUERY_WITH_FACET_UNSELECTED
            && FacetedNavigationUtils.isFacetSelected(facet, selectedCategoryValues)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Can we use the counts from the unscoped query?
     * 
     * We can do this for single select unselect other facets as the count from the unscoped query
     * will be the same as selecting any value which will first remove any facets (like the unscoped query)
     * and then apply a single constraint (which we can count from the unscoped query).
     * @param facet
     * @param searchTransaction
     * @return
     */
    public boolean useUnscopedQueryForCounts(FacetDefinition facet, SearchTransaction searchTransaction) {
        if(facet.getConstraintJoin() == FacetConstraintJoin.LEGACY) {
            return false;
        }
        
        return facet.getSelectionType() == FacetSelectionType.SINGLE_AND_UNSELECT_OTHER_FACETS;
    }
    
}
