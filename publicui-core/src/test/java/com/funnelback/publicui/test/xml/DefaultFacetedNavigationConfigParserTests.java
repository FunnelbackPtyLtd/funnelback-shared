package com.funnelback.publicui.test.xml;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.common.facetednavigation.marshaller.xml.FacetMarshallerXml.FacetLocation;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.DateFieldFill;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.GScopeItem;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.MetadataFieldFill;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.URLFill;
import com.funnelback.publicui.xml.DefaultFacetedNavigationConfigParser;
import com.funnelback.publicui.xml.FacetedNavigationConfigParser.FacetedNavigationConfigParseException;
import com.funnelback.publicui.xml.FacetedNavigationConfigParser.Facets;

public class DefaultFacetedNavigationConfigParserTests {

    private Facets facets;
    
    @Before
    public void before() throws IOException, FacetedNavigationConfigParseException {
        DefaultFacetedNavigationConfigParser parser = new DefaultFacetedNavigationConfigParser();
        facets = parser.parseFacetedNavigationConfiguration(FileUtils.readFileToByteArray(new File("src/test/resources/faceted-navigation/sample-config.xml")), 
            FacetLocation.INDEX);
        Assert.assertNotNull(facets);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSameName() throws IOException, FacetedNavigationConfigParseException {
        DefaultFacetedNavigationConfigParser parser = new DefaultFacetedNavigationConfigParser();
        parser.parseFacetedNavigationConfiguration(FileUtils.readFileToByteArray(new File("src/test/resources/faceted-navigation/same-name-facets.xml")),
            FacetLocation.INDEX);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testInvalidXml() throws FacetedNavigationConfigParseException {
        new DefaultFacetedNavigationConfigParser().parseFacetedNavigationConfiguration("<Facets><Facet><Data></Data><MetadataFieldFill></Facet></Facets>".getBytes(),
            FacetLocation.INDEX);
    }
    
    @Test
    public void testFacets() {
        Assert.assertEquals(9, facets.facetDefinitions.size());
    }
    
    @Test
    public void testFacet1() {
        FacetDefinition facet = facets.facetDefinitions.get(0);
        assertEquals("Industry", facet.getName());
        assertEquals(1, facet.getCategoryDefinitions().size());
        
        MetadataFieldFill c = (MetadataFieldFill) facet.getCategoryDefinitions().get(0);
        
        assertEquals("Z", c.getMetadataClass());
        
        assertEquals(1, c.getSubCategories().size());
        
        c = (MetadataFieldFill) c.getSubCategories().get(0);
        
        assertEquals("Y", c.getMetadataClass());
        Assert.assertEquals(facet.getName(), c.getFacetName());
    }
    
    @Test
    public void testFacet2And3And4() {
        FacetDefinition facet = facets.facetDefinitions.get(1);
        assertEquals("State", facet.getName());
        assertEquals(1, facet.getCategoryDefinitions().size());
        
        MetadataFieldFill c = (MetadataFieldFill) facet.getCategoryDefinitions().get(0);
        assertEquals("X", c.getMetadataClass());
        assertEquals(0, c.getSubCategories().size());
        Assert.assertEquals(facet.getName(), c.getFacetName());
        
        facet = facets.facetDefinitions.get(2);
        assertEquals("Source", facet.getName());
        assertEquals(1, facet.getCategoryDefinitions().size());
        
        c = (MetadataFieldFill) facet.getCategoryDefinitions().get(0);
        assertEquals("W", c.getMetadataClass());
        assertEquals(0, c.getSubCategories().size());
        Assert.assertEquals(facet.getName(), c.getFacetName());

        facet = facets.facetDefinitions.get(3);
        assertEquals("Date Posted", facet.getName());
        assertEquals(1, facet.getCategoryDefinitions().size());
        
        c = (MetadataFieldFill) facet.getCategoryDefinitions().get(0);
        assertEquals("V", c.getMetadataClass());
        assertEquals(0, c.getSubCategories().size());
        Assert.assertEquals(facet.getName(), c.getFacetName());
    }
    
    @Test
    public void testFacet5() {
        FacetDefinition facet = facets.facetDefinitions.get(4);
        assertEquals("Pre built categories", facet.getName());
        assertEquals(3, facet.getCategoryDefinitions().size());
        
        GScopeItem c = (GScopeItem) facet.getCategoryDefinitions().get(0);
        assertEquals("Writing Jobs", c.getData());
        assertEquals("66", c.getGScopeNumber());
        assertEquals(0, c.getSubCategories().size());

        c = (GScopeItem) facet.getCategoryDefinitions().get(1);
        assertEquals("Seaside Jobs", c.getData());
        assertEquals("2", c.getGScopeNumber());
        assertEquals(0, c.getSubCategories().size());

        GScopeItem c2 = (GScopeItem) facet.getCategoryDefinitions().get(2);
        assertEquals("Fruit picking jobs", c2.getData());
        assertEquals("3", c2.getUserSetGScope());
        assertEquals(0, c.getSubCategories().size());
    }
    
    @Test
    public void testFacet6() {
        FacetDefinition facet = facets.facetDefinitions.get(5);
        assertEquals("Jobs by author", facet.getName());
        assertEquals(2, facet.getCategoryDefinitions().size());
        
        MetadataFieldFill c1 = (MetadataFieldFill) facet.getCategoryDefinitions().get(0);
        assertEquals("a", c1.getData());
        assertEquals(0, c1.getSubCategories().size());
        
        MetadataFieldFill c2 = (MetadataFieldFill) facet.getCategoryDefinitions().get(1);
        assertEquals("U", c2.getMetadataClass());
        assertEquals(0, c1.getSubCategories().size());
    }
    
    @Test
    public void testFacet7And8() {
        FacetDefinition facet = facets.facetDefinitions.get(6);
        assertEquals("New jobs", facet.getName());
        assertEquals(1, facet.getCategoryDefinitions().size());
        
        URLFill c1 = (URLFill) facet.getCategoryDefinitions().get(0);
        // Trailing slash must have been aded
        assertEquals("http://example.com/jobs/new/", c1.getData());
        assertEquals(0, c1.getSubCategories().size());
        
        facet = facets.facetDefinitions.get(7);
        assertEquals("Old jobs", facet.getName());
        assertEquals(1, facet.getCategoryDefinitions().size());
        
        c1 = (URLFill) facet.getCategoryDefinitions().get(0);
        assertEquals("http://example.com/jobs/old/", c1.getData());
        assertEquals(0, c1.getSubCategories().size());
    }
    
    @Test
    public void testFacet9() {
        FacetDefinition facet = facets.facetDefinitions.get(8);
        assertEquals("Date-based facet", facet.getName());
        assertEquals(1, facet.getCategoryDefinitions().size());
        
        DateFieldFill dff = (DateFieldFill) facet.getCategoryDefinitions().get(0);
        assertEquals("d", dff.getData());
    }
}

