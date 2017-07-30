package com.funnelback.publicui.test.search.model.collection.facetednavigation;

import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import static java.util.Arrays.asList;

import com.funnelback.common.facetednavigation.models.FacetConstraintJoin;
import com.funnelback.common.facetednavigation.models.FacetValues;
import com.funnelback.publicui.search.model.collection.QueryProcessorOption;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition.FacetSearchData;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition.MetadataAndValue;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryValueComputedDataHolder;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetExtraSearchNames;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import static org.mockito.Mockito.*;
import static com.funnelback.publicui.search.model.transaction.SearchTransaction.ExtraSearches.FACETED_NAVIGATION;
public class CategoryDefinitionTests {

    @Test
    public void testParseMetadata() {
        MetadataAndValue mv = CategoryDefinition.parseMetadata("a:bcd");
        Assert.assertEquals("a", mv.metadata);
        Assert.assertEquals("bcd", mv.value);
        
        mv = CategoryDefinition.parseMetadata("a:bcd efg");
        Assert.assertEquals("a", mv.metadata);
        Assert.assertEquals("bcd efg", mv.value);

        mv = CategoryDefinition.parseMetadata("a:bcd efg\"h");
        Assert.assertEquals("a", mv.metadata);
        Assert.assertEquals("bcd efg\"h", mv.value);

        mv = CategoryDefinition.parseMetadata("X:yz");
        Assert.assertEquals("X", mv.metadata);
        Assert.assertEquals("yz", mv.value);
        
        mv = CategoryDefinition.parseMetadata("J k:lm no");
        Assert.assertEquals("J k", mv.metadata);
        Assert.assertEquals("lm no", mv.value);
        
        mv = CategoryDefinition.parseMetadata(":value");
        Assert.assertEquals("", mv.metadata);
        Assert.assertEquals("value", mv.value);
        
        mv = CategoryDefinition.parseMetadata("a:");
        Assert.assertEquals("a", mv.metadata);
        Assert.assertEquals("", mv.value);
        
        mv = CategoryDefinition.parseMetadata("");
        Assert.assertEquals(null, mv.metadata);
        Assert.assertEquals(null, mv.value);

        mv = CategoryDefinition.parseMetadata(null);
        Assert.assertEquals(null, mv.metadata);
        Assert.assertEquals(null, mv.metadata);

    }
    
    @Test
    public void getFacetSearchDataTestAndFromScoped() {
        FacetDefinition fdef = mock(FacetDefinition.class);
        when(fdef.getFacetValues()).thenReturn(FacetValues.FROM_SCOPED_QUERY);
        when(fdef.getConstraintJoin()).thenReturn(FacetConstraintJoin.AND);
        
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), new SearchResponse());
        SearchTransaction extraSearchTransaction = new SearchTransaction(new SearchQuestion(), new SearchResponse());
        st.getExtraSearches().put(FACETED_NAVIGATION.toString(), extraSearchTransaction);
        
        FacetSearchData data = new MockCategoryDefinition("").getFacetSearchData(st, fdef);
        
        Assert.assertEquals(st.getResponse(), data.getResponseForCounts().apply(null, null).get());
        Assert.assertEquals(st.getResponse(), data.getResponseForValues());
        Assert.assertNull(data.getCountIfNotPresent().apply(null, null));
    }
    
    @Test
    public void getFacetSearchDataTestLegacyFromUnScoped() {
        FacetDefinition fdef = mock(FacetDefinition.class);
        when(fdef.getFacetValues()).thenReturn(FacetValues.FROM_UNSCOPED_QUERY);
        when(fdef.getConstraintJoin()).thenReturn(FacetConstraintJoin.LEGACY);
        
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), new SearchResponse());
        SearchTransaction extraSearchTransaction = new SearchTransaction(new SearchQuestion(), new SearchResponse());
        st.getExtraSearches().put(FACETED_NAVIGATION.toString(), extraSearchTransaction);
        
        FacetSearchData data = new MockCategoryDefinition("").getFacetSearchData(st, fdef);
        
        // Legacy has everything come from the main search packet, as legacy did
        // not support unscoped query.
        Assert.assertEquals(st.getResponse(), data.getResponseForCounts().apply(null, null).get());
        Assert.assertEquals(st.getResponse(), data.getResponseForValues());
        // Don't worry about default count as we wont need one legacy.
    }
    
    
    @Test
    public void getFacetSearchDataTestOrFromScoped() {
        FacetDefinition fdef = mock(FacetDefinition.class);
        when(fdef.getName()).thenReturn("fname");
        when(fdef.getFacetValues()).thenReturn(FacetValues.FROM_SCOPED_QUERY);
        when(fdef.getConstraintJoin()).thenReturn(FacetConstraintJoin.OR);
        
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), new SearchResponse());
        SearchTransaction extraSearchTransaction = new SearchTransaction(new SearchQuestion(), new SearchResponse());
        st.getExtraSearches().put(FACETED_NAVIGATION.toString(), extraSearchTransaction);
        
        MockCategoryDefinition catDef = new MockCategoryDefinition("") {
            @Override
            public String getQueryStringParamName() {
                return "foobar";
            }
            
        };
        FacetSearchData data = catDef.getFacetSearchData(st, fdef);
        
        Assert.assertEquals("Counts in the OR case are not supported.",
            false, data.getResponseForCounts().apply(null, null).isPresent());
        Assert.assertEquals(st.getResponse(), data.getResponseForValues());
        
        Assert.assertNull(data.getCountIfNotPresent().apply(catDef, ""));
    }
    
    @Test
    public void getFacetSearchDataTestOrFromUnScoped() {
        FacetDefinition fdef = mock(FacetDefinition.class);
        when(fdef.getName()).thenReturn("facet-name");
        when(fdef.getFacetValues()).thenReturn(FacetValues.FROM_UNSCOPED_QUERY);
        when(fdef.getConstraintJoin()).thenReturn(FacetConstraintJoin.OR);
        
        MockCategoryDefinition catDef = new MockCategoryDefinition("cat-name"){
            @Override
            public String getQueryStringParamName() {
                return "foobar";
            }
            
        };
        
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), new SearchResponse());
        SearchTransaction extraSearchTransaction = new SearchTransaction(new SearchQuestion(), new SearchResponse());
        st.getExtraSearches().put(FACETED_NAVIGATION.toString(), extraSearchTransaction);
        
        FacetExtraSearchNames facetExtraSearchNames = new FacetExtraSearchNames();
        
        SearchTransaction selectedValueExtraSearch = mock(SearchTransaction.class, Mockito.RETURNS_DEEP_STUBS);
        
        when(selectedValueExtraSearch.getResponse().getResultPacket().getResultsSummary().getTotalMatching())
            .thenReturn(1337);
        
        st.getExtraSearches().put(facetExtraSearchNames.getExtraSearchName(fdef, catDef, "val"), 
            selectedValueExtraSearch);
        
        FacetSearchData data = catDef.getFacetSearchData(st, fdef);
        
        Assert.assertEquals("Counts in the OR case are not supported.",
            false, data.getResponseForCounts().apply(null, null).isPresent());
        Assert.assertEquals("Values come from the unscoped query",
            extraSearchTransaction.getResponse(), data.getResponseForValues());
        
        //Counts should come from a very specifc extra search.
        Assert.assertEquals(1337, data.getCountIfNotPresent().apply(catDef, "val") + 0);
    }
    
    @Test
    public void getFacetSearchDataTestAndFromUnScoped() {
        FacetDefinition fdef = mock(FacetDefinition.class);
        when(fdef.getFacetValues()).thenReturn(FacetValues.FROM_UNSCOPED_QUERY);
        when(fdef.getConstraintJoin()).thenReturn(FacetConstraintJoin.AND);
        
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), new SearchResponse());
        SearchTransaction extraSearchTransaction = new SearchTransaction(new SearchQuestion(), new SearchResponse());
        st.getExtraSearches().put(FACETED_NAVIGATION.toString(), extraSearchTransaction);
        
        FacetSearchData data = new MockCategoryDefinition("").getFacetSearchData(st, fdef);
        
        Assert.assertEquals("Counts in the AND case come from the main query because we scope the"
            + " existing query further.",
            st.getResponse(), data.getResponseForCounts().apply(null, null).get());
        Assert.assertEquals("Values come from the unscoped query",
            extraSearchTransaction.getResponse(), data.getResponseForValues());
        Assert.assertEquals("The default count is zero, if the value from the unscoped query is not in"
            + " the scopped query then ANDing with that value will result in a zero result page.",
            0, data.getCountIfNotPresent().apply(null, null) + 0);
    }
    
    
    
    public class MockCategoryDefinition extends CategoryDefinition {
        
        public MockCategoryDefinition(String data) {
            super(data);
        }

        @Override
        public List<CategoryValueComputedDataHolder> computeData(SearchTransaction st, FacetDefinition fdef) {
            throw new NotImplementedException("not mocked");
        }

        @Override
        public String getQueryStringParamName() {
            throw new NotImplementedException("not mocked");
        }

        @Override
        public boolean matches(String value, String extraParams) {
            throw new NotImplementedException("not mocked");
        }

        @Override
        public List<QueryProcessorOption<?>> getQueryProcessorOptions(SearchQuestion question) {
            throw new NotImplementedException("not mocked");
        }
        
        @Override
        public FacetSearchData getFacetSearchData(SearchTransaction st, FacetDefinition facetDefinition) {
            return super.getFacetSearchData(st, facetDefinition);
        }
        
    }
    
    public class CategoryDefinitionComputeData extends MockCategoryDefinition {
        
        public CategoryDefinitionComputeData(String data, List<CategoryValueComputedDataHolder> computedData) {
            super(data);
            this.computedData = computedData;
        }

        public List<CategoryValueComputedDataHolder> computedData;
        
        @Override
        public List<CategoryValueComputedDataHolder> computeData(SearchTransaction st, FacetDefinition fdef) {
            return computedData;
        }
        
    }
    
    @Test
    public void testComputeValues() {
        CategoryValueComputedDataHolder data = new CategoryValueComputedDataHolder("dhawking", 
            "David", 12, "constraint?", false, "f.author|", "author=dhawking");
        List<CategoryValue> values = new CategoryDefinitionComputeData("d", asList(data)).computeValues(null, null);
        Assert.assertEquals(1, values.size());
        
        
        Assert.assertEquals("dhawking", values.get(0).getData());
        Assert.assertEquals("David", values.get(0).getLabel());
        Assert.assertEquals(12, 0 + values.get(0).getCount());
        Assert.assertEquals("constraint?", values.get(0).getConstraint());
        Assert.assertEquals(false, values.get(0).isSelected());
        Assert.assertEquals("f.author|", values.get(0).getQueryStringParamName());
        Assert.assertEquals("author=dhawking", values.get(0).getQueryStringParamValue());
        
        Assert.assertEquals("f.author%7C=author%3Ddhawking", values.get(0).getQueryStringParam());
    }
    
}

