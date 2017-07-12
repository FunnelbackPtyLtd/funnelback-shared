package com.funnelback.publicui.test.search.model.collection.facetednavigation;

import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.Assert;
import org.junit.Test;
import static java.util.Arrays.asList;

import com.funnelback.common.facetednavigation.models.FacetConstraintJoin;
import com.funnelback.common.facetednavigation.models.FacetValues;
import com.funnelback.publicui.search.model.collection.QueryProcessorOption;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition.FacetSearchData;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition.MetadataAndValue;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryValueComputedDataHolder;
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
    public void getFacetSearchDataTest() {
        FacetDefinition fdef = mock(FacetDefinition.class);
        when(fdef.getFacetValues()).thenReturn(FacetValues.FROM_SCOPED_QUERY);
        when(fdef.getConstraintJoin()).thenReturn(FacetConstraintJoin.AND);
        
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), new SearchResponse());
        SearchTransaction extraSearchTransaction = new SearchTransaction(new SearchQuestion(), new SearchResponse());
        st.getExtraSearches().put(FACETED_NAVIGATION.toString(), extraSearchTransaction);
        
        FacetSearchData data = new MockCategoryDefinition("").getFacetSearchData(st, fdef);
        
        // TODO something?
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

