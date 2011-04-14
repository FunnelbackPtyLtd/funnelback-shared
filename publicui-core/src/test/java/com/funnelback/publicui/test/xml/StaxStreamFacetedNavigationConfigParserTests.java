package com.funnelback.publicui.test.xml;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.GScopeItem;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.MetadataFieldFill;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.MetadataTypeFill;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.QueryItem;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.URLFill;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.XPathFill;
import com.funnelback.publicui.xml.FacetedNavigationConfigParser.Facets;
import com.funnelback.publicui.xml.StaxStreamFacetedNavigationConfigParser;
import com.funnelback.publicui.xml.XmlParsingException;

public class StaxStreamFacetedNavigationConfigParserTests {

	private Facets facets;
	
	@Before
	public void before() throws IOException, XmlParsingException {
		StaxStreamFacetedNavigationConfigParser parser = new StaxStreamFacetedNavigationConfigParser();
		facets = parser.parseFacetedNavigationConfiguration(FileUtils.readFileToString(new File("src/test/resources/faceted-navigation/sample-config.xml")));
		Assert.assertNotNull(facets);
	}
	
	@Test(expected=XmlParsingException.class)
	public void testInvalidXml() throws XmlParsingException {
		new StaxStreamFacetedNavigationConfigParser().parseFacetedNavigationConfiguration("<Facets><Facet><Data></Data><MetadataFieldFill></Facet></Facets>");
	}
	
	@Test
	public void testQpOptions() {
		Assert.assertEquals("-rmcfabcd", facets.qpOptions);
	}
	
	@Test
	public void testFacets() {
		Assert.assertEquals(7, facets.facetDefinitions.size());
	}
	
	@Test
	public void testFacet1() {
		FacetDefinition facet = facets.facetDefinitions.get(0);
		assertEquals("Industry", facet.getName());
		assertEquals(1, facet.getCategoryDefinitions().size());
		
		XPathFill c = (XPathFill) facet.getCategoryDefinitions().get(0);
		assertEquals("/CATEGORY", c.getData());
		assertEquals("Z", c.getMetafield());
		
		assertEquals(1, c.getSubCategories().size());
		
		c = (XPathFill) c.getSubCategories().get(0);
		assertEquals("/SUBCATEGORY", c.getData());
		assertEquals("Y", c.getMetafield());
	}
	
	@Test
	public void testFacet2And3And4() {
		FacetDefinition facet = facets.facetDefinitions.get(1);
		assertEquals("State", facet.getName());
		assertEquals(1, facet.getCategoryDefinitions().size());
		
		XPathFill c = (XPathFill) facet.getCategoryDefinitions().get(0);
		assertEquals("/STATE", c.getData());
		assertEquals("X", c.getMetafield());
		assertEquals(0, c.getSubCategories().size());
		
		facet = facets.facetDefinitions.get(2);
		assertEquals("Source", facet.getName());
		assertEquals(1, facet.getCategoryDefinitions().size());
		
		c = (XPathFill) facet.getCategoryDefinitions().get(0);
		assertEquals("/COMPANY_NAME", c.getData());
		assertEquals("W", c.getMetafield());
		assertEquals(0, c.getSubCategories().size());

		facet = facets.facetDefinitions.get(3);
		assertEquals("Date Posted", facet.getName());
		assertEquals(1, facet.getCategoryDefinitions().size());
		
		c = (XPathFill) facet.getCategoryDefinitions().get(0);
		assertEquals("/DATE_STRING", c.getData());
		assertEquals("V", c.getMetafield());
		assertEquals(0, c.getSubCategories().size());
	}
	
	@Test
	public void testFacet5() {
		FacetDefinition facet = facets.facetDefinitions.get(4);
		assertEquals("Pre built categories", facet.getName());
		assertEquals(3, facet.getCategoryDefinitions().size());
		
		QueryItem c = (QueryItem) facet.getCategoryDefinitions().get(0);
		assertEquals("Writing Jobs", c.getData());
		assertEquals("author writer journalist", c.getQuery());
		assertEquals(0, c.getSubCategories().size());

		c = (QueryItem) facet.getCategoryDefinitions().get(1);
		assertEquals("Seaside Jobs", c.getData());
		assertEquals("coast sea water", c.getQuery());
		assertEquals(0, c.getSubCategories().size());

		GScopeItem c2 = (GScopeItem) facet.getCategoryDefinitions().get(2);
		assertEquals("Fruit picking jobs", c2.getData());
		assertEquals(3, c2.getUserSetGScope());
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
		
		MetadataTypeFill c2 = (MetadataTypeFill) facet.getCategoryDefinitions().get(1);
		assertEquals("jobs.author", c2.getData());
		assertEquals("U", c2.getMetafield());
		assertEquals(0, c1.getSubCategories().size());
	}
	
	@Test
	public void testFacet7() {
		FacetDefinition facet = facets.facetDefinitions.get(6);
		assertEquals("New jobs", facet.getName());
		assertEquals(1, facet.getCategoryDefinitions().size());
		
		URLFill c1 = (URLFill) facet.getCategoryDefinitions().get(0);
		assertEquals("http://example.com/jobs/new", c1.getData());
		assertEquals(0, c1.getSubCategories().size());
	}
}

