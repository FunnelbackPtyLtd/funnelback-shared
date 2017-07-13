package com.funnelback.publicui.test.contentauditor;

import static com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition.getFacetWithDefaults;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.contentauditor.MissingMetadataFill;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.lifecycle.output.processors.FacetedNavigation;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.config.DefaultConfigRepository;
import com.funnelback.publicui.xml.padre.StaxStreamParser;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class FacetedNavigationOutputMissingMetadataFillTests {

    private final File searchHome = new File("src/test/resources/dummy-search_home/");
    
    @Resource(name="localConfigRepository")
    private DefaultConfigRepository configRepository;
    
    private SearchTransaction st;
    private FacetedNavigation processor;

    @Before
    public void before() throws Exception {
        SearchQuestion question = new SearchQuestion();
        // Re-use same Collection as metadata-field-fill
        question.setCollection(configRepository.getCollection("faceted-navigation-metadatafieldfill"));
        
        SearchResponse response = new SearchResponse();
        // Re-use same XML as xpath-fill
        response.setResultPacket(new StaxStreamParser().parse(
            FileUtils.readFileToByteArray(new File("src/test/resources/padre-xml/faceted-navigation-xpathfill.xml")),
            StandardCharsets.UTF_8,
            false));
        st = new SearchTransaction(question, response);

        // We set this in code here because MissingMetadataFill is
        // not intended to be permitted in the faceted navigation config file.
        List<FacetDefinition> facetDefinitions = new ArrayList<FacetDefinition>();
        MissingMetadataFill categoryDefinition = new MissingMetadataFill();
        categoryDefinition.setFacetName("Missing Metadata");
        facetDefinitions.add(getFacetWithDefaults("Missing Metadata", Arrays.asList(new CategoryDefinition[]{categoryDefinition})));
        
        question.getCollection().setFacetedNavigationConfConfig(new FacetedNavigationConfig(facetDefinitions));

        processor = new FacetedNavigation();
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
        sq.setCollection(new Collection("dummy", new NoOptionsConfig(searchHome, "dummy")));
        processor.processOutput(new SearchTransaction(sq, response));
    }

    @Test
    public void test() throws OutputProcessorException {
        Assert.assertEquals(0, st.getResponse().getFacets().size());
        processor.processOutput(st);
        
        Assert.assertEquals(1, st.getResponse().getFacets().size());
        
        Facet f = st.getResponse().getFacets().get(0);
        Assert.assertEquals("Missing Metadata", f.getName());
        Assert.assertEquals(1, f.getCategories().size());

        Facet.Category c = f.getCategories().get(0);
        Assert.assertNull(c.getLabel());
        Assert.assertEquals("f.Missing Metadata|missing", c.getQueryStringParamName());
        Assert.assertEquals(4, c.getValues().size());
        
        Facet.CategoryValue cv = c.getValues().get(0);
        Assert.assertEquals("missing", cv.getConstraint());
        Assert.assertEquals(7, cv.getCount() + 0);
        Assert.assertEquals("X", cv.getData());
        Assert.assertEquals("X", cv.getLabel());
        Assert.assertEquals("f.Missing+Metadata%7Cmissing=X", cv.getQueryStringParam());
        Assert.assertFalse(cv.isSelected());
    }

    @Test
    public void testSelected() throws OutputProcessorException {
        List<String> selected = new ArrayList<String>();
        selected.add("X");
        st.getQuestion().getSelectedCategoryValues().put("f.Missing Metadata|missing", selected);
        
        Assert.assertEquals(0, st.getResponse().getFacets().size());
        processor.processOutput(st);
        
        Assert.assertEquals(1, st.getResponse().getFacets().size());
        
        Facet f = st.getResponse().getFacets().get(0);
        Assert.assertEquals("Missing Metadata", f.getName());
        Assert.assertEquals(1, f.getCategories().size());

        Facet.Category c = f.getCategories().get(0);
        Assert.assertNull(c.getLabel());
        Assert.assertEquals("f.Missing Metadata|missing", c.getQueryStringParamName());
        Assert.assertEquals(4, c.getValues().size());
        
        Facet.CategoryValue cv = c.getValues().get(0);
        Assert.assertEquals("missing", cv.getConstraint());
        Assert.assertEquals(7, cv.getCount() + 0);
        Assert.assertEquals("X", cv.getData());
        Assert.assertEquals("X", cv.getLabel());
        Assert.assertEquals("f.Missing+Metadata%7Cmissing=X", cv.getQueryStringParam());
        Assert.assertTrue(cv.isSelected());
    }

}
