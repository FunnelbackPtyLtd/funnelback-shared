package com.funnelback.publicui.test.contentauditor;

import java.io.File;
import java.io.FileNotFoundException;
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
import com.funnelback.common.system.EnvironmentVariableException;
import com.funnelback.publicui.contentauditor.MissingMetadataFill;
import com.funnelback.publicui.search.lifecycle.input.processors.FacetedNavigation;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.config.DefaultConfigRepository;
import com.funnelback.publicui.xml.padre.StaxStreamParser;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class FacetedNavigationInputMissingMetadataFillTests {

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
        facetDefinitions.add(new FacetDefinition("Missing Metadata", Arrays.asList(new CategoryDefinition[]{categoryDefinition})));
        
        question.getCollection().setFacetedNavigationConfConfig(new FacetedNavigationConfig("-rmcf=ZWXYUV", facetDefinitions));

        processor = new FacetedNavigation();
    }
    
    @Test
    public void testMissingData() throws FileNotFoundException, EnvironmentVariableException {
        FacetedNavigation processor = new FacetedNavigation();
        
        // No transaction
        processor.processInput(null);
        
        // No question
        SearchTransaction st = new SearchTransaction(null, null);
        processor.processInput(st);
        Assert.assertNull(st.getQuestion());
        
        // No collection
        SearchQuestion question = new SearchQuestion();
        st = new SearchTransaction(question, null);
        processor.processInput(st);
        Assert.assertEquals(0, st.getQuestion().getDynamicQueryProcessorOptions().size());
        
        // No faceted navigation config
        question.setCollection(new Collection("dummy", new NoOptionsConfig("dummy")));
        st = new SearchTransaction(question, null);
        processor.processInput(st);
        Assert.assertEquals(0, st.getQuestion().getDynamicQueryProcessorOptions().size());
        
        // No QP Options
        Collection c = new Collection("dummy", new NoOptionsConfig("dummy"));
        c.setFacetedNavigationLiveConfig(new FacetedNavigationConfig(null, null));
        question.setCollection(c);
        st = new SearchTransaction(question, null);
        processor.processInput(st);
        Assert.assertEquals(0, st.getQuestion().getDynamicQueryProcessorOptions().size());
    
    }

    @Test
    public void test() {        
        processor.processInput(st);
        
        Assert.assertEquals(1, st.getQuestion().getDynamicQueryProcessorOptions().size());
        Assert.assertEquals("-rmcf=ZWXYUV", st.getQuestion().getDynamicQueryProcessorOptions().get(0));
    }

    @Test
    public void testEmpty() {
        st.getQuestion().getRawInputParameters().put("f.Missing Metadata|missing", new String[0]);
        processor.processInput(st);
        Assert.assertEquals(0, st.getQuestion().getFacetsQueryConstraints().size());
        
        st.getQuestion().getRawInputParameters().put("f.Missing Metadata|missing", new String[] {""});
        processor.processInput(st);
        Assert.assertEquals(0, st.getQuestion().getFacetsQueryConstraints().size());
    }

    @Test
    public void testFacetSelected() {
        Assert.assertEquals(0, st.getQuestion().getFacetsQueryConstraints().size());
        Assert.assertNull(st.getQuestion().getFacetsGScopeConstraints());
        
        st.getQuestion().getRawInputParameters().put("f.Missing Metadata|missing", new String[] {"X"});
        processor.processInput(st);
        
        Assert.assertNull(st.getQuestion().getFacetsGScopeConstraints());
        Assert.assertEquals(1, st.getQuestion().getFacetsQueryConstraints().size());
        Assert.assertEquals("|-X:$++", st.getQuestion().getFacetsQueryConstraints().get(0));
    }

    @Test
    public void testSameName() {
        Assert.assertEquals(0, st.getQuestion().getFacetsQueryConstraints().size());
        Assert.assertNull(st.getQuestion().getFacetsGScopeConstraints());
        
        st.getQuestion().getRawInputParameters().put("f.Missing Metadata|missing", new String[] {"X"});
        processor.processInput(st);
        
        Assert.assertNull(st.getQuestion().getFacetsGScopeConstraints());
        Assert.assertEquals(1, st.getQuestion().getFacetsQueryConstraints().size());
        Assert.assertEquals("|-X:$++", st.getQuestion().getFacetsQueryConstraints().get(0));
    }
    
}
