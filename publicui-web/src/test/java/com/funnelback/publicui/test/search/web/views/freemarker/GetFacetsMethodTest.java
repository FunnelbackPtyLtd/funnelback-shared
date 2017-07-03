package com.funnelback.publicui.test.search.web.views.freemarker;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;

import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.web.views.freemarker.GetFacetsMethod;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModelException;
import junit.framework.Assert;
public class GetFacetsMethodTest {

    public class TestableGetFacetsMethod extends GetFacetsMethod {
        protected Object unwrapArgument(Object arg) throws TemplateModelException {
            return arg;
        }
    }
    
    @Test
    public void testNoFacets() throws Exception {
        SearchResponse sr = mock(SearchResponse.class);
        List<Object> args = asList(sr);
        
        List<Facet> result = new TestableGetFacetsMethod().execMethod(args);
        Assert.assertEquals(0, result.size());
    }
    
    @Test
    public void testAllFacets() throws Exception {
        SearchResponse sr = new SearchResponse();
        sr.getFacets().add(makeFacetWithName("planet"));
        
        List<Object> args = asList(sr);
        
        List<Facet> result = new TestableGetFacetsMethod().execMethod(args);
        Assert.assertEquals(1, result.size());
    }
    
    @Test
    public void testAllFacetsReturnedWithEmptyString() throws Exception {
        SearchResponse sr = new SearchResponse();
        sr.getFacets().add(makeFacetWithName("planet"));
        
        List<Object> args = asList(sr, SimpleScalar.newInstanceOrNull(""));
        
        List<Facet> result = new TestableGetFacetsMethod().execMethod(args);
        Assert.assertEquals(1, result.size());
    }
    
    @Test
    public void testNoFacetsReturnedWithSingleComma() throws Exception {
        SearchResponse sr = new SearchResponse();
        sr.getFacets().add(makeFacetWithName("planet"));
        
        List<Object> args = asList(sr, SimpleScalar.newInstanceOrNull(","));
        
        List<Facet> result = new TestableGetFacetsMethod().execMethod(args);
        Assert.assertEquals(0, result.size());
    }
    
    @Test
    public void testSelectingFacets() throws Exception {
        SearchResponse sr = new SearchResponse();
        sr.getFacets().add(makeFacetWithName("country"));
        sr.getFacets().add(makeFacetWithName("planet"));
        sr.getFacets().add(makeFacetWithName("tab"));
        
        List<Object> args = asList(sr, SimpleScalar.newInstanceOrNull("planet, country"));
        
        List<Facet> result = new TestableGetFacetsMethod().execMethod(args);
        Assert.assertEquals(2, result.size());
        Assert.assertEquals("planet", result.get(0).getName());
        Assert.assertEquals("country", result.get(1).getName());
    }
    
    private Facet makeFacetWithName(String name) {
        Facet facet = mock(Facet.class);
        when(facet.getName()).thenReturn(name);
        return facet;
    }
}
