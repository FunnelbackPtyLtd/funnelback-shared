package com.funnelback.publicui.test.contentauditor;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Assert;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.contentauditor.CountThresholdMetadataFieldFill;
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
import com.funnelback.publicui.test.search.lifecycle.output.processors.FacetedNavigationAbstractMetadataFieldFillTestMethods;
import com.funnelback.publicui.xml.padre.StaxStreamParser;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class FacetedNavigationOutputCountThresholdMetadataFieldFillTests extends FacetedNavigationAbstractMetadataFieldFillTestMethods {

    @Resource(name="localConfigRepository")
    private DefaultConfigRepository configRepository;
    
    @Before
    public void before() throws Exception {
        SearchQuestion question = new SearchQuestion();
        question.setCollection(configRepository.getCollection("faceted-navigation-metadatafieldfill"));
        
        SearchResponse response = new SearchResponse();
        // Re-use same XML as xpath-fill
        response.setResultPacket(new StaxStreamParser().parse(
            FileUtils.readFileToString(new File("src/test/resources/padre-xml/faceted-navigation-xpathfill.xml")),
            false));
        st = new SearchTransaction(question, response);

        // We set this in code here because CountThresholdMetadataFieldFill is
        // not intended to be permitted in the faceted navigation config file.
        question.getCollection().setFacetedNavigationConfConfig(FacetedNavigationCountThresholdMetadataFieldFillTestUtils.buildFacetConfig());

        processor = new FacetedNavigation();
    }
    
    /** 
     * Builds a facet config matching faceted-navigation-metadatafieldfill's
     * but with CountThresholdMetadataFieldFill instead of straight MetadataFieldFill
     * category definitions.
     */
    private FacetedNavigationConfig buildFacetConfig(SearchQuestion question) {

        List<FacetDefinition> facetDefinitions = new ArrayList<FacetDefinition>();
        
        List<CategoryDefinition> categories = 
            metadataFillWithThresholdDefition("Z", "Location", 0, 
                metadataFillWithThresholdDefition("Y", "Location", 0, 
                    metadataFillWithThresholdDefition("X", "Location", 0, new ArrayList<>())
                )
            );
        categories.addAll(metadataFillWithThresholdDefition("O", "Location", 0, new ArrayList<>()));
        facetDefinitions.add(new FacetDefinition("Location", categories));
        
        facetDefinitions.add(new FacetDefinition("Job Category",  
            metadataFillWithThresholdDefition("W", "Job Category", 1, 
                metadataFillWithThresholdDefition("V", "Job Category", 0, new ArrayList<>())
            )
        ));

        facetDefinitions.add(new FacetDefinition("Type",  
            metadataFillWithThresholdDefition("U", "Type", 0, new ArrayList<>())
        ));
        
        FacetedNavigationConfig facetedNavigationConfig = new FacetedNavigationConfig("-rmcf=ZWXYUV", facetDefinitions);
        return facetedNavigationConfig;
    }
    
    /** Convenience method to build a CountThresholdMetadataFieldFill with the given settings. */
    private List<CategoryDefinition> metadataFillWithThresholdDefition(String metadataClass, String facetName, int threshold, List<CategoryDefinition> subCategory) {
        CountThresholdMetadataFieldFill result = new CountThresholdMetadataFieldFill(1);
        result.setData(metadataClass);
        result.setThreshold(threshold);
        result.setFacetName(facetName);
        result.getSubCategories().clear();
        result.getSubCategories().addAll(subCategory);
        
        return new ArrayList<>(Arrays.asList(new CategoryDefinition[]{result}));
        // Why wrap again with ArrayList? Because otherwise addAll throws an Unsupported exception - weird
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
        
        // No sub-categories should be returned since nothing
        // has been selected in the first level category
        Assert.assertEquals(0, c.getCategories().size());
        
        f = st.getResponse().getFacets().get(1);
        Assert.assertEquals("Job Category", f.getName());
        Assert.assertEquals(1, f.getCategories().size());
        
        c = f.getCategories().get(0);
        Assert.assertNull(c.getLabel());
        Assert.assertEquals("f.Job Category|W", c.getQueryStringParamName());

        // This is the only bit of the actual test which differs from the normal metadata field fill one
        // We now expect only one entry, not two (because one will be filtered).
        Assert.assertEquals(1, c.getValues().size());
        
        cv = c.getValues().get(0);
        Assert.assertEquals("W", cv.getConstraint());
        Assert.assertEquals(26, cv.getCount());
        Assert.assertEquals("divtrades & servicesdiv", cv.getData());
        Assert.assertEquals("divtrades & servicesdiv", cv.getLabel());
        Assert.assertEquals("f.Job+Category%7CW=divtrades+%26+servicesdiv", cv.getQueryStringParam());
    }
        
}
