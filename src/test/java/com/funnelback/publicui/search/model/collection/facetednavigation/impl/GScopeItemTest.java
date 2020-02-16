package com.funnelback.publicui.search.model.collection.facetednavigation.impl;

import static com.funnelback.common.facetednavigation.models.FacetValues.FROM_UNSCOPED_ALL_QUERY;
import static com.funnelback.common.facetednavigation.models.FacetValues.FROM_UNSCOPED_QUERY;
import static com.funnelback.publicui.search.model.collection.facetednavigation.FacetExtraSearchNames.SEARCH_FOR_ALL_VALUES;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.common.facetednavigation.models.FacetConstraintJoin;
import com.funnelback.common.facetednavigation.models.FacetSelectionType;
import com.funnelback.common.facetednavigation.models.FacetValues;
import com.funnelback.publicui.search.model.collection.QueryProcessorOption;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition.FacetSearchData;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryValueComputedDataHolder;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.google.common.collect.Sets;

import lombok.Setter;
public class GScopeItemTest {
    
    public class TestableGScopeItem extends GScopeItem {
        
        @Setter private FacetSearchData facetSearchData;

        public TestableGScopeItem(String categoryName, String userSetGscope) {
            super(categoryName, userSetGscope);
        }
        
        protected FacetSearchData getFacetSearchData(SearchTransaction st, FacetDefinition facetDefinition) {
            return this.facetSearchData;
        }
        
    }

    private GScopeItem category;
    
    @Before
    public void before() {
        category = new GScopeItem("categoryName", "12");
        category.setFacetName("facetName");
        category.setLabel("label");
    }
    
    @Test
    public void testFields() {
        Assert.assertEquals("categoryName", category.getData());
        Assert.assertEquals("12", category.getGScopeNumber());
        Assert.assertEquals("12", category.getUserSetGScope());
        Assert.assertEquals("facetName", category.getFacetName());
        Assert.assertEquals("label", category.getLabel());
        
        Assert.assertEquals(Sets.newHashSet("f.facetName|12"), category.getAllQueryStringParamNames());
    }
    
    @Test
    public void testConstraint() {
        Assert.assertEquals("12", category.getGScope1Constraint());
    }
    
    @Test
    public void testQPOs() {
        List<QueryProcessorOption<?>> actual = category.getQueryProcessorOptions(null);
        Assert.assertEquals(Collections.singletonList(new QueryProcessorOption<>("countgbits", "all")), actual);
    }
    
    @Test
    public void testWhenNoGscopeMatch() {
        SearchResponse sr = new SearchResponse();
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), sr);
        
        sr.setResultPacket(new ResultPacket());
        sr.getResultPacket().getGScopeCounts().put("1", 12);
        
        FacetSearchData facetSearchData = new FacetSearchData(sr, 
            (c, v) -> Optional.empty(), 
            (c,v) -> null); // This part is where the count might come from an extra search.
    
        TestableGScopeItem testableGScopeItem = new TestableGScopeItem("News", "2");
        
        testableGScopeItem.setFacetSearchData(facetSearchData);
        
        Assert.assertEquals("We should have no values because we have no count for the gscope",
            0, 
            testableGScopeItem.computeData(st, facetWithValue(FROM_UNSCOPED_QUERY)).size());
    }
    
    @Test
    public void testWhenNoGscopeMatchInAllValuesFacet() {
        SearchResponse sr = new SearchResponse();
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), sr);
        
        sr.setResultPacket(new ResultPacket());
        sr.getResultPacket().getGScopeCounts().put("1", 12);
        
        FacetSearchData facetSearchData = new FacetSearchData(sr, 
            (c, v) -> Optional.empty(), 
            (c,v) -> null); // This part is where the count might come from an extra search.
    
        TestableGScopeItem testableGScopeItem = new TestableGScopeItem("News", "2");
        
        testableGScopeItem.setFacetSearchData(facetSearchData);
        
        Assert.assertEquals(
            "We should have a value because the facet type requires we show all values",
            1, 
            testableGScopeItem.computeData(st, facetWithValue(FROM_UNSCOPED_ALL_QUERY)).size());
    }
    
    @Test
    public void testWhenNoGscopeMatchTabsTest() {
        SearchResponse sr = new SearchResponse();
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), sr);
        
        sr.setResultPacket(new ResultPacket());
        
        st.getExtraSearches().put(SEARCH_FOR_ALL_VALUES, 
            new SearchTransaction(new SearchQuestion(), new SearchResponse()));
        
        st.getExtraSearches().get(SEARCH_FOR_ALL_VALUES).getResponse()
            .setResultPacket(new ResultPacket());
        
    
        GScopeItem testableGScopeItem = new GScopeItem("News", "2");
        
        FacetDefinition facetDef = facetWithValue(FROM_UNSCOPED_ALL_QUERY);
        // This makes it a tab:
        when(facetDef.getSelectionType()).thenReturn(FacetSelectionType.SINGLE_AND_UNSELECT_OTHER_FACETS);
        when(facetDef.getConstraintJoin()).thenReturn(FacetConstraintJoin.AND);
        
        List<CategoryValueComputedDataHolder> values
         = testableGScopeItem.computeData(st, facetWithValue(FROM_UNSCOPED_ALL_QUERY));
        
        Assert.assertEquals(
            "We should have a value because the facet type requires we show all values",
            1, values.size());
        
        
        
        Assert.assertEquals(new Integer(0), values.get(0).getCount());
    }
    
    @Test
    public void testWhenCountForTabsCase() {
        SearchResponse sr = new SearchResponse();
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), sr);
        
        sr.setResultPacket(new ResultPacket());
        st.getResponse().getResultPacket().getGScopeCounts().put("2", 12);
        
        st.getExtraSearches().put(SEARCH_FOR_ALL_VALUES, 
            new SearchTransaction(new SearchQuestion(), new SearchResponse()));
        
        st.getExtraSearches().get(SEARCH_FOR_ALL_VALUES).getResponse()
            .setResultPacket(new ResultPacket());
        
        st.getExtraSearches().get(SEARCH_FOR_ALL_VALUES).getResponse().getResultPacket()
            .getGScopeCounts().put("2", 100);
        
    
        GScopeItem testableGScopeItem = new GScopeItem("News", "2");
        
        FacetDefinition facetDef = facetWithValue(FROM_UNSCOPED_ALL_QUERY);
        // This makes it a tab:
        when(facetDef.getSelectionType()).thenReturn(FacetSelectionType.SINGLE_AND_UNSELECT_OTHER_FACETS);
        when(facetDef.getConstraintJoin()).thenReturn(FacetConstraintJoin.AND);
        
        List<CategoryValueComputedDataHolder> values
         = testableGScopeItem.computeData(st, facetWithValue(FROM_UNSCOPED_ALL_QUERY));
        
        Assert.assertEquals(1, values.size());
        
        
        
        Assert.assertEquals(new Integer(12), values.get(0).getCount());
    }
    
    @Test
    public void addsMissingSelectedValues() {
        SearchResponse sr = new SearchResponse();
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), sr);
        sr.setResultPacket(new ResultPacket());
        
        GScopeItem gscopeItem = new GScopeItem("Bars", "gs1");
        gscopeItem.setFacetName("FacetName");
        // Select the category.
        st.getQuestion().getInputParameterMap().put(gscopeItem.getQueryStringParamName(), "Bars");
        
        List<CategoryValueComputedDataHolder> values = gscopeItem.computeData(st, facetWithValue(FROM_UNSCOPED_QUERY));
        Assert.assertEquals("Although no values are known from the result packet as this is selected, "
            + "we should have faked the value", 1, values.size());
        Assert.assertTrue(values.get(0).isSelected());
        Assert.assertEquals(0, values.get(0).getCount() + 0);
    }
    
    private FacetDefinition facetWithValue(FacetValues facetValue) {
        FacetDefinition facetDefinition = mock(FacetDefinition.class);
        when(facetDefinition.getFacetValues()).thenReturn(facetValue);
        return facetDefinition;
    }
    
}
