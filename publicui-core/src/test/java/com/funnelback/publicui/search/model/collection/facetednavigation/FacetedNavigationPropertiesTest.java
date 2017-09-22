package com.funnelback.publicui.search.model.collection.facetednavigation;
import static com.funnelback.common.facetednavigation.models.FacetValues.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import com.funnelback.common.facetednavigation.models.FacetConstraintJoin;
import com.funnelback.common.facetednavigation.models.FacetSelectionType;
import com.funnelback.common.facetednavigation.models.FacetValues;
import com.funnelback.common.function.StreamUtils;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.*;
import static com.funnelback.common.facetednavigation.models.FacetConstraintJoin.*;
import static com.funnelback.common.facetednavigation.models.FacetValues.*;
import static com.funnelback.publicui.search.model.collection.facetednavigation.FacetExtraSearchNames.SEARCH_FOR_UNSCOPED_VALUES;
import static com.funnelback.common.facetednavigation.models.FacetSelectionType.*;
import com.funnelback.publicui.utils.FacetedNavigationUtils;
public class FacetedNavigationPropertiesTest {

    private final FacetedNavigationProperties facetedNavProps = new FacetedNavigationProperties();
    
    @Test
    public void needsDedicatedSearchTestExtraSearchRequired() {
        FacetDefinition facetDef = facet("Extras", OR, FROM_UNSCOPED_QUERY, MULTIPLE);
        SearchTransaction st = getSearchTransaction(Pair.of("Extras", "radio"));
        
        Assert.assertTrue("As the facet is of type OR with multiple select and the facet has been selected, "
            + "then selecting another value will result in expanding the result set so a dedicated extra search "
            + "is need.", 
            facetedNavProps.useDedicatedExtraSearchForCounts(facetDef, st));
    }
    
    @Test
    public void needsDedicatedSearchTestFacetMustBeSelected() {
        FacetDefinition facetDef = facet("Extras", OR, FROM_UNSCOPED_QUERY, MULTIPLE);
        SearchTransaction st = getSearchTransaction();
        
        Assert.assertFalse("No extra searches needed as the facet is not selected, selecting the facet"
            + " would reduce the scope so the normal query can be used to get counts.", 
            facetedNavProps.useDedicatedExtraSearchForCounts(facetDef, st));
    }
    
    @Test
    public void needsDedicatedSearchTestAndTypeFacet() {
        FacetDefinition facetDef = facet("Extras", AND, FROM_UNSCOPED_QUERY, MULTIPLE);
        SearchTransaction st = getSearchTransaction(Pair.of("Extras", "radio"));
        
        Assert.assertFalse("The values are jouned by AND so selecting another reduces the result set, "
            + "so the normal scoped search can be used to get counts.", 
            facetedNavProps.useDedicatedExtraSearchForCounts(facetDef, st));
    }
    
    @Test
    public void needsDedicatedSearchTestRequiredInSingleCase() {
        FacetDefinition facetDef = facet("Extras", AND, FROM_UNSCOPED_QUERY, SINGLE);
        SearchTransaction st = getSearchTransaction(Pair.of("Extras", "radio"));
        
        Assert.assertFalse("Radio type does not need dedicated searches per value.", 
            facetedNavProps.useDedicatedExtraSearchForCounts(facetDef, st));
    }
    
    @Test
    public void radioUsesScopedWithUncheckedFacetSearch() {
        // Radio type facets want to get counts by running a scopped search with only radio
        // facet unchecked.
        FacetDefinition radioFacet = facet("radio", AND, FROM_UNSCOPED_QUERY, SINGLE);
        SearchTransaction radioSelectedSearchTransaction = getSearchTransaction(Pair.of("radio", "radio"));
        
        Assert.assertFalse("If the radio is not selected we can use the scoped query to get counts.", 
            facetedNavProps.useScopedSearchWithFacetDisabledForCounts(radioFacet, getSearchTransaction()));
        
        FacetDefinition radioAllValuesFacet = facet("radio", AND, FROM_UNSCOPED_ALL_QUERY, SINGLE);
        
        Assert.assertTrue("If the radio is selected we need to unselect the radio to get the counts", 
            facetedNavProps.useScopedSearchWithFacetDisabledForCounts(radioAllValuesFacet, radioSelectedSearchTransaction));
        
        FacetDefinition radioFacetDisabled = facet("radio", AND, FROM_SCOPED_QUERY_WITH_FACET_UNSELECTED, SINGLE);
        
        Assert.assertTrue(facetedNavProps
                .useScopedSearchWithFacetDisabledForCounts(
                        radioFacetDisabled, radioSelectedSearchTransaction));
    }
    
    @Test
    public void needsDedicatedSearchTestMustBeSelected() {
        FacetDefinition facetDef = facet("Extras", AND, FROM_UNSCOPED_QUERY, SINGLE);
        SearchTransaction st = getSearchTransaction();
        
        Assert.assertFalse("Selecting this facet will reduce the result set so the normal query can"
            + " be used to get counts.", 
            facetedNavProps.useDedicatedExtraSearchForCounts(facetDef, st));
    }
    
    @Test
    public void needsDedicatedSearchTestLegacy() {
        FacetDefinition facetDef = facet("Extras", LEGACY, FROM_UNSCOPED_QUERY, SINGLE);
        SearchTransaction st = getSearchTransaction(Pair.of("Extras", "radio"));
        
        Assert.assertFalse("Legacy did not support extra searches.", 
            facetedNavProps.useDedicatedExtraSearchForCounts(facetDef, st));
    }
    
    @Test
    public void needsDedicatedSearchTestStandardDrillDownFacets() {
        FacetDefinition facetDef = facet("Extras", AND, FROM_SCOPED_QUERY, SINGLE);
        SearchTransaction st = getSearchTransaction(Pair.of("Extras", "radio"));
        
        Assert.assertFalse("In standard drill down facets we don't need extra searches.", 
            facetedNavProps.useDedicatedExtraSearchForCounts(facetDef, st));
    }
    
    @Test
    public void useUnscopedQueryForCountsTest() {
        FacetDefinition facetDef = facet("Extras", OR, FROM_UNSCOPED_QUERY, FacetSelectionType.SINGLE_AND_UNSELECT_OTHER_FACETS);
        SearchTransaction st = getSearchTransaction(Pair.of("Extras", "radio"));
        
        Assert.assertTrue(facetedNavProps.useUnscopedQueryForCounts(facetDef, st));
        
        Assert.assertFalse("We don't need a dedicated extra search as the unscoped search can be used instead.", 
            facetedNavProps.useDedicatedExtraSearchForCounts(facetDef, st));
        
    }
    
    SearchTransaction getSearchTransaction(Pair<String, String> ... selectedCategoryValues) {
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), new SearchResponse());
        
        StreamUtils.ofNullable(selectedCategoryValues)
            .forEach(p -> st.getQuestion().getSelectedCategoryValues().put(
                FacetedNavigationUtils.facetParamNamePrefix(p.getKey()), 
                asList(p.getValue())));
        
        return st;
    }
    
    private FacetDefinition facet(String name, 
        FacetConstraintJoin join,
        FacetValues values,
        FacetSelectionType selectionType) {
        FacetDefinition facetDef = mock(FacetDefinition.class);
        when(facetDef.getConstraintJoin()).thenReturn(join);
        when(facetDef.getName()).thenReturn(name);
        when(facetDef.getSelectionType()).thenReturn(selectionType);
        when(facetDef.getFacetValues()).thenReturn(values);
        return facetDef;
    }
}
