package com.funnelback.publicui.search.model.collection.facetednavigation.impl;

import static com.funnelback.common.facetednavigation.models.FacetValues.FROM_UNSCOPED_QUERY;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.common.facetednavigation.models.FacetValues;
import com.funnelback.publicui.search.model.collection.QueryProcessorOption;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryValueComputedDataHolder;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.google.common.collect.Sets;

public class MetadataFieldFillTest {

    private MetadataFieldFill category;
    
    @Before
    public void before() {
        category = new MetadataFieldFill("author");
        category.setFacetName("facetName");
        category.setLabel("label");
    }
    
    @Test
    public void testFields() {
        Assert.assertEquals("author", category.getData());
        Assert.assertEquals("author", category.getMetadataClass());
        Assert.assertEquals("facetName", category.getFacetName());
        Assert.assertEquals("label", category.getLabel());
        
        Assert.assertEquals(Sets.newHashSet("f.facetName|author"), category.getAllQueryStringParamNames());
    }
    
    @Test
    public void testConstraint() {
        Assert.assertEquals("author:\"$++ a value $++\"", category.getQueryConstraint("a value"));
    }
    
    @Test
    public void testQPOs() {
        List<QueryProcessorOption<?>> actual = category.getQueryProcessorOptions(null);
        Assert.assertEquals(Collections.singletonList(new QueryProcessorOption<>("rmcf", "author")), actual);
    }
    
    @Test
    public void addsMissingSelectedValues() {
        SearchResponse sr = new SearchResponse();
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), sr);
        sr.setResultPacket(new ResultPacket());
        
        MetadataFieldFill metadataItem = new MetadataFieldFill("a");
        metadataItem.setFacetName("FacetName");
        // Select the category.
        st.getQuestion().getInputParameterMap().put(metadataItem.getQueryStringParamName(), "Bob");
        
        List<CategoryValueComputedDataHolder> values = metadataItem.computeData(st, facetWithValue(FROM_UNSCOPED_QUERY));
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
