package com.funnelback.publicui.test.search.lifecycle.output.processors;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Assert;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.lifecycle.output.processors.FacetedNavigation;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.config.DefaultConfigRepository;
import com.funnelback.publicui.xml.padre.StaxStreamParser;

import lombok.Setter;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class FacetedNavigationGScopesTests {

    @Resource(name="localConfigRepository")
    private DefaultConfigRepository configRepository;

    @Autowired
    @Setter
    protected File searchHome;
    
    private SearchTransaction st;
    private FacetedNavigation processor;
    
    @Before
    public void before() throws Exception {
        SearchQuestion question = new SearchQuestion();
        question.setCollection(configRepository.getCollection("faceted-navigation-gscopes"));
        
        st = new SearchTransaction(question, new SearchResponse());
        
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
    public void test() throws Exception {
        
        st.getResponse().setResultPacket(new StaxStreamParser().parse(
            FileUtils.readFileToByteArray(new File("src/test/resources/padre-xml/faceted-navigation-gscopes.xml")),
            StandardCharsets.UTF_8,
            false));
        
        Assert.assertEquals(0, st.getResponse().getFacets().size());
        processor.processOutput(st);
        
        Assert.assertEquals(1, st.getResponse().getFacets().size());
        
        Facet f = st.getResponse().getFacets().get(0);
        Assert.assertEquals("By Story", f.getName());
        Assert.assertEquals(5, f.getCategories().size());

        // First category: Full, gscope=40
        Facet.Category c = f.getCategories().get(0);
        Assert.assertNull(c.getLabel());
        Assert.assertEquals("f.By Story|40", c.getQueryStringParamName());
        Assert.assertEquals(1, c.getValues().size());
        
        Facet.CategoryValue cv = c.getValues().get(0);
        Assert.assertEquals("40", cv.getConstraint());
        Assert.assertEquals(108, cv.getCount() + 0 + 0);
        Assert.assertEquals("40", cv.getData());
        Assert.assertEquals("Full", cv.getLabel());
        Assert.assertEquals("f.By+Story%7C40=Full", cv.getQueryStringParam());
        Assert.assertFalse(cv.isSelected());
        
        // No sub-categories should be returned since nothing
        // has been selected in the first level category
        Assert.assertEquals(0, c.getCategories().size());
        
        // Last category: Henry IV, gscope=1
        c = f.getCategories().get(3);
        Assert.assertNull(c.getLabel());
        Assert.assertEquals("f.By Story|1", c.getQueryStringParamName());
        Assert.assertEquals(1, c.getValues().size());
        
        cv = c.getValues().get(0);
        Assert.assertEquals("1", cv.getConstraint());
        Assert.assertEquals(22, cv.getCount() + 0 + 0);
        Assert.assertEquals("1", cv.getData());
        Assert.assertEquals("Henry IV", cv.getLabel());
        Assert.assertEquals("f.By+Story%7C1=Henry+IV", cv.getQueryStringParam());
        Assert.assertFalse(cv.isSelected());
        
        Assert.assertEquals(0, c.getCategories().size());
        
    }
    
    @Test
    public void testCategorySelection() throws Exception {
        st.getResponse().setResultPacket(new StaxStreamParser().parse(
            FileUtils.readFileToByteArray(new File("src/test/resources/padre-xml/faceted-navigation-gscopes-selected.xml")),
            StandardCharsets.UTF_8,
            false));
        
        Assert.assertEquals(0, st.getResponse().getFacets().size());
        
        List<String> selected = new ArrayList<String>();
        selected.add("Cleopatra");
        st.getQuestion().getSelectedCategoryValues().put("f.By Story|8", selected);
        processor.processOutput(st);
        
        Assert.assertEquals(1, st.getResponse().getFacets().size());
        
        Facet f = st.getResponse().getFacets().get(0);
        Assert.assertEquals("By Story", f.getName());
        Assert.assertEquals(1, f.getCategories().size());

        // First category: Cleopatra, gscope=8
        Facet.Category c = f.getCategories().get(0);
        Assert.assertNull(c.getLabel());
        Assert.assertEquals("f.By Story|8", c.getQueryStringParamName());
        Assert.assertEquals(1, c.getValues().size());
        
        Facet.CategoryValue cv = c.getValues().get(0);
        Assert.assertEquals("8", cv.getConstraint());
        Assert.assertEquals(46, cv.getCount() + 0);
        Assert.assertEquals("8", cv.getData());
        Assert.assertEquals("Cleopatra", cv.getLabel());
        Assert.assertEquals("f.By+Story%7C8=Cleopatra", cv.getQueryStringParam());
        Assert.assertTrue(cv.isSelected());
        
        Assert.assertEquals(0, c.getCategories().size());        
    }
    
    @Test
    public void testNestedCategory() throws Exception {
        st.getResponse().setResultPacket(new StaxStreamParser().parse(
            FileUtils.readFileToByteArray(new File("src/test/resources/padre-xml/faceted-navigation-gscopes-nested.xml")),
            StandardCharsets.UTF_8,
            false));
        
        Assert.assertEquals(0, st.getResponse().getFacets().size());
        
        List<String> selected = new ArrayList<String>();
        selected.add("Coriolanus");
        st.getQuestion().getSelectedCategoryValues().put("f.By Story|10", selected);
        processor.processOutput(st);
        
        Assert.assertEquals(1, st.getResponse().getFacets().size());
        
        Facet f = st.getResponse().getFacets().get(0);
        Assert.assertEquals("By Story", f.getName());
        Assert.assertEquals(1, f.getCategories().size());

        // First category: Coriolanus, gscope=10
        Facet.Category c = f.getCategories().get(0);
        Assert.assertNull(c.getLabel());
        Assert.assertEquals("f.By Story|10", c.getQueryStringParamName());
        Assert.assertEquals(1, c.getValues().size());
        
        Facet.CategoryValue cv = c.getValues().get(0);
        Assert.assertEquals("10", cv.getConstraint());
        Assert.assertEquals(33, cv.getCount() + 0);
        Assert.assertEquals("10", cv.getData());
        Assert.assertEquals("Coriolanus", cv.getLabel());
        Assert.assertEquals("f.By+Story%7C10=Coriolanus", cv.getQueryStringParam());
        Assert.assertTrue(cv.isSelected());
        
        Assert.assertEquals(1, c.getCategories().size());
        
        // Second category: Full, gscope=40
        c = c.getCategories().get(0);
        Assert.assertNull(c.getLabel());
        Assert.assertEquals("f.By Story|40", c.getQueryStringParamName());
        Assert.assertEquals(1, c.getValues().size());
        
        cv = c.getValues().get(0);
        Assert.assertEquals("40", cv.getConstraint());
        Assert.assertEquals(3, cv.getCount() + 0);
        Assert.assertEquals("40", cv.getData());
        Assert.assertEquals("Full (nested)", cv.getLabel());
        Assert.assertEquals("f.By+Story%7C40=Full+%28nested%29", cv.getQueryStringParam());
        Assert.assertFalse(cv.isSelected());
        
        Assert.assertEquals(0, c.getCategories().size());

    }
        
}
