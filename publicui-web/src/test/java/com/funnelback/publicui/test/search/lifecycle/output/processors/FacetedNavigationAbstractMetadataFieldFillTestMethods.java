package com.funnelback.publicui.test.search.lifecycle.output.processors;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.lifecycle.output.processors.FacetedNavigation;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class FacetedNavigationAbstractMetadataFieldFillTestMethods {

    protected SearchTransaction st;
    protected FacetedNavigation processor;

    public FacetedNavigationAbstractMetadataFieldFillTestMethods() {
        super();
    }

    @Test
    public void testMissingData() throws Exception {
        // No transaction
        processor.processOutput(null);
        
        // No response
        processor.processOutput(new SearchTransaction(null, null));
        
        // No results
        SearchResponse response = new SearchResponse();
        processor.processOutput(new SearchTransaction(null, response));
        
        // No results in packet
        response.setResultPacket(new ResultPacket());
        processor.processOutput(new SearchTransaction(null, response));
        
        // No collection
        processor.processOutput(new SearchTransaction(new SearchQuestion(), response));
        
        // Collection but no config
        SearchQuestion sq = new SearchQuestion();
        sq.setCollection(new Collection("dummy", null));
        processor.processOutput(new SearchTransaction(sq, response));
        
        // Config but no faceted_nav. config
        sq = new SearchQuestion();
        sq.setCollection(new Collection("dummy", new NoOptionsConfig("dummy")));
        processor.processOutput(new SearchTransaction(sq, response));
    }

    @Test
    public void test() throws OutputProcessorException {
        Assert.assertEquals(0, st.getResponse().getFacets().size());
        processor.processOutput(st);
        
        Assert.assertEquals(3, st.getResponse().getFacets().size());
        
        Facet f = st.getResponse().getFacets().get(0);
        Assert.assertEquals("Location", f.getName());
        Assert.assertEquals(1, f.getCategories().size());
    
        // First category: Location=australia
        Facet.Category c = f.getCategories().get(0);
        Assert.assertNull(c.getLabel());
        Assert.assertEquals("f.Location|Z", c.getQueryStringParamName());
        Assert.assertEquals(1, c.getValues().size());
        
        Facet.CategoryValue cv = c.getValues().get(0);
        Assert.assertEquals("Z", cv.getConstraint());
        Assert.assertEquals(27, cv.getCount());
        Assert.assertEquals("australia", cv.getData());
        Assert.assertEquals("australia", cv.getLabel());
        Assert.assertEquals("f.Location%7CZ=australia", cv.getQueryStringParam());
        Assert.assertFalse(cv.isSelected());
        
        // No sub-categories should be returned since nothing
        // has been selected in the first level category
        Assert.assertEquals(0, c.getCategories().size());
        
        f = st.getResponse().getFacets().get(1);
        Assert.assertEquals("Job Category", f.getName());
        Assert.assertEquals(1, f.getCategories().size());
        
        c = f.getCategories().get(0);
        Assert.assertNull(c.getLabel());
        Assert.assertEquals("f.Job Category|W", c.getQueryStringParamName());
        Assert.assertEquals(2, c.getValues().size());
        
        cv = c.getValues().get(0);
        Assert.assertEquals("W", cv.getConstraint());
        Assert.assertEquals(26, cv.getCount());
        Assert.assertEquals("divtrades & servicesdiv", cv.getData());
        Assert.assertEquals("divtrades & servicesdiv", cv.getLabel());
        Assert.assertEquals("f.Job+Category%7CW=divtrades+%26+servicesdiv", cv.getQueryStringParam());
        Assert.assertFalse(cv.isSelected());
        
    }

    @Test
    public void testNestedCategorySelection() {
        Assert.assertEquals(0, st.getResponse().getFacets().size());
        
        List<String> selected = new ArrayList<String>();
        selected.add("australia");
        st.getQuestion().getSelectedCategoryValues().put("f.Location|Z", selected);
        processor.processOutput(st);
        
        Assert.assertEquals(3, st.getResponse().getFacets().size());
        
        Facet f = st.getResponse().getFacets().get(0);
        Assert.assertEquals("Location", f.getName());
        Assert.assertEquals(1, f.getCategories().size());
    
        // First category: Location=australia
        Facet.Category c = f.getCategories().get(0);
        Assert.assertNull(c.getLabel());
        Assert.assertEquals("f.Location|Z", c.getQueryStringParamName());
        Assert.assertEquals(1, c.getValues().size());
        
        Facet.CategoryValue cv = c.getValues().get(0);
        Assert.assertEquals("Z", cv.getConstraint());
        Assert.assertEquals(27, cv.getCount());
        Assert.assertEquals("australia", cv.getData());
        Assert.assertEquals("australia", cv.getLabel());
        Assert.assertEquals("f.Location%7CZ=australia", cv.getQueryStringParam());
        Assert.assertTrue(cv.isSelected());
        
        // Nested category: Location= australia + state
        Assert.assertEquals(1, c.getCategories().size());
        Facet.Category subCategory = c.getCategories().get(0);
        Assert.assertNull(subCategory.getLabel());
        Assert.assertEquals("f.Location|Y", subCategory.getQueryStringParamName());
        Assert.assertEquals(5, subCategory.getValues().size());
    
        cv = subCategory.getValues().get(2);
        Assert.assertEquals("Y", cv.getConstraint());
        Assert.assertEquals(5, cv.getCount());
        Assert.assertEquals("nsw", cv.getData());
        Assert.assertEquals("nsw", cv.getLabel());
        Assert.assertEquals("f.Location%7CY=nsw", cv.getQueryStringParam());
        Assert.assertFalse(cv.isSelected());
        
        // Nested-nested category: Location = australia + state + city
        // shouldn't be selected
        Assert.assertEquals(0, subCategory.getCategories().size());        
    }

}