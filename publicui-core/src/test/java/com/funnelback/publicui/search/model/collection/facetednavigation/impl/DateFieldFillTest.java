package com.funnelback.publicui.search.model.collection.facetednavigation.impl;

import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.publicui.search.model.collection.QueryProcessorOption;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryValueComputedDataHolder;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.padre.DateCount;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.google.common.collect.Sets;

public class DateFieldFillTest {

    private DateFieldFill category;
    
    @Before
    public void before() {
        category = new DateFieldFill("d");
        category.setFacetName("facetName");
        category.setLabel("label");
    }
    
    @Test
    public void testFields() {
        Assert.assertEquals("d", category.getData());
        Assert.assertEquals("d", category.getMetadataClass());
        Assert.assertEquals("facetName", category.getFacetName());
        Assert.assertEquals("label", category.getLabel());
        
        Assert.assertEquals(Sets.newHashSet("f.facetName|d"), category.getAllQueryStringParamNames());
    }
    
    @Test
    public void testConstraint() {
        Assert.assertEquals("a value", category.getQueryConstraint("a value"));
    }
    
    @Test
    public void testQPOs() {
        List<QueryProcessorOption<?>> actual = category.getQueryProcessorOptions(null);
        Assert.assertEquals(Collections.singletonList(new QueryProcessorOption<>("count_dates", "d")), actual);
    }
    
    @Test
    public void addsMissingSelectedValues() {
        FacetDefinition facetDef = mock(FacetDefinition.class);
        
        DateFieldFill dateFill = new DateFieldFill("d");
        dateFill.setFacetName("FacetName");
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), new SearchResponse());
        st.getResponse().setResultPacket(new ResultPacket());
        
        st.getQuestion().getInputParameterMap().put(dateFill.getQueryStringParamName(), "d<2017");
        List<CategoryValueComputedDataHolder> values = dateFill.computeData(st, facetDef);
        Assert.assertEquals(1, values.size());
        
        Assert.assertTrue(values.get(0).isSelected());
        Assert.assertEquals(0, values.get(0).getCount() + 0);
        Assert.assertEquals("We show the constraint as we don't know how to work out the padre label from the constraint.",
            "d<2017", values.get(0).getLabel());
    }
    
    @Test
    public void addsMissingSelectedValuesWithLabel() {
        FacetDefinition facetDef = mock(FacetDefinition.class);
        
        DateFieldFill dateFill = new DateFieldFill("d");
        dateFill.setFacetName("FacetName");
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), new SearchResponse());
        st.getResponse().setResultPacket(new ResultPacket());
        
        String queryConstraint = "d<2017";
        // We support the label having the constraint and label seperator
        String cgiValue = dateFill.constructCGIValue(queryConstraint, DateFieldFill.CONSTRAINT_AND_LABEL_SEPARATOR + "Hello");
        
        Assert.assertEquals(queryConstraint, dateFill.getQueryConstraint(cgiValue));
        
        st.getQuestion().getInputParameterMap().put(dateFill.getQueryStringParamName(), cgiValue);
        List<CategoryValueComputedDataHolder> values = dateFill.computeData(st, facetDef);
        Assert.assertEquals(1, values.size());
        
        Assert.assertTrue(values.get(0).isSelected());
        Assert.assertEquals(0, values.get(0).getCount() + 0);
        Assert.assertEquals("The label is in the CGI value so we should be able to extract it.",
            DateFieldFill.CONSTRAINT_AND_LABEL_SEPARATOR + "Hello", values.get(0).getLabel());
    }
    
    @Test
    public void testPadreReturnsDateCount() {
        FacetDefinition facetDef = mock(FacetDefinition.class);
        
        DateFieldFill dateFill = new DateFieldFill("d");
        dateFill.setFacetName("FacetName");
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), new SearchResponse());
        st.getResponse().setResultPacket(new ResultPacket());
        
        String queryConstraint = "d<2017";
        
        Map<String, DateCount> dates = st.getResponse().getResultPacket().getDateCounts();
        dates.put("d:Hello", new DateCount(queryConstraint, 1000));
        
        
        String cgiValue = dateFill.constructCGIValue(queryConstraint, "Hello");
        
        Assert.assertEquals(queryConstraint, dateFill.getQueryConstraint(cgiValue));
        
        // Simulate the date as being selected.
        st.getQuestion().getInputParameterMap().put(dateFill.getQueryStringParamName(), cgiValue);
        st.getQuestion().getSelectedCategoryValues().put(dateFill.getQueryStringParamName(), Arrays.asList(cgiValue));
        
        List<CategoryValueComputedDataHolder> values = dateFill.computeData(st, facetDef);
        Assert.assertEquals(1, values.size());
        
        Assert.assertTrue(values.get(0).isSelected());
        Assert.assertEquals(1000, values.get(0).getCount() + 0);
        Assert.assertEquals("The label is in the CGI value so we should be able to extract it.",
            "Hello", values.get(0).getLabel());
    }
    
    
}
