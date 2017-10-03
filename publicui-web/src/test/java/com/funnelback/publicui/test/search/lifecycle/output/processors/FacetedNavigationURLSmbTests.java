package com.funnelback.publicui.test.search.lifecycle.output.processors;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.publicui.search.lifecycle.output.processors.FacetedNavigation;
import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.Facet.Category;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.config.DefaultConfigRepository;
import com.funnelback.publicui.xml.padre.StaxStreamParser;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class FacetedNavigationURLSmbTests {

    @Resource(name="localConfigRepository")
    private DefaultConfigRepository configRepository;

    private SearchTransaction st;
    private FacetedNavigation processor;
    
    @Before
    public void before() throws Exception {
        SearchQuestion question = new SearchQuestion();
        question.setCollection(configRepository.getCollection("faceted-navigation-urls-smb"));
        
        st = new SearchTransaction(question, new SearchResponse());
        
        processor = new FacetedNavigation();
    }
    
    @Test
    public void test() throws Exception {
        
        st.getResponse().setResultPacket(new StaxStreamParser().parse(
            FileUtils.readFileToByteArray(new File("src/test/resources/padre-xml/faceted-navigation-urls-smb.xml")),
            StandardCharsets.UTF_8,
            false));
        
        Assert.assertEquals(0, st.getResponse().getFacets().size());
        processor.processOutput(st);
        
        Assert.assertEquals(1, st.getResponse().getFacets().size());
        
        Facet f = st.getResponse().getFacets().get(0);
        Assert.assertEquals("By URL", f.getName());
        Assert.assertEquals(1, f.getCategories().size());

        Facet.Category c = f.getCategories().get(0);
        Assert.assertNull(c.getLabel());
        Assert.assertEquals("f.By URL|url", c.getQueryStringParamName());
        Assert.assertEquals(37, c.getValues().size());
        
        Facet.CategoryValue cv = c.getValues().get(0);
        Assert.assertEquals("v", cv.getConstraint());
        Assert.assertEquals(46, cv.getCount() + 0);
        Assert.assertEquals("cleopatra", cv.getData());
        Assert.assertEquals("cleopatra", cv.getLabel());
        Assert.assertEquals("f.By+URL%7Curl=share%2FShakespeare%2Fcleopatra", cv.getQueryStringParam());
        Assert.assertFalse(cv.isSelected());

        // Check a category with encoded strings
        cv = c.getValues().get(1);
        Assert.assertEquals("v", cv.getConstraint());
        Assert.assertEquals(44, cv.getCount() + 0);
        Assert.assertEquals("with spaces & ampersand", cv.getData());
        Assert.assertEquals("with spaces & ampersand", cv.getLabel());
        Assert.assertEquals("f.By+URL%7Curl=share%2FShakespeare%2Fwith%2520spaces%2520%2526%2520ampersand", cv.getQueryStringParam());

        // No sub-categories should be returned since nothing
        // has been selected in the first level category
        Assert.assertEquals(0, c.getCategories().size());
        
    }
    
    @Test
    public void testCategorySelection() throws Exception {
        st.getResponse().setResultPacket(new StaxStreamParser().parse(FileUtils.readFileToByteArray(
            new File("src/test/resources/padre-xml/faceted-navigation-urls-smb-selected.xml")),
            StandardCharsets.UTF_8,
            false));
        
        Assert.assertEquals(0, st.getResponse().getFacets().size());
        
        List<String> selected = new ArrayList<String>();
        selected.add("cleopatra");
        st.getQuestion().getSelectedCategoryValues().put("f.By URL|url", selected);

        processor.processOutput(st);
        
        Assert.assertEquals(1, st.getResponse().getFacets().size());
        
        Facet f = st.getResponse().getFacets().get(0);
        Assert.assertEquals("By URL", f.getName());
        Assert.assertEquals(1, f.getCategories().size());
        
        Category c = f.getCategories().get(0);
        Assert.assertTrue(c.getCategories().isEmpty());
        Assert.assertEquals("f.By URL|url", c.getQueryStringParamName());
        
        Assert.assertEquals(1, c.getValues().size());
        CategoryValue cv = c.getValues().get(0);
        Assert.assertEquals("v", cv.getConstraint());
        Assert.assertEquals(46, cv.getCount() + 0);
        Assert.assertEquals("cleopatra", cv.getData());
        Assert.assertEquals("cleopatra", cv.getLabel());
        Assert.assertEquals("f.By+URL%7Curl=share%2FShakespeare%2Fcleopatra", cv.getQueryStringParam());
        Assert.assertTrue(cv.isSelected());
    }
    
    @Test
    public void testCategorySelectionEncoded() throws Exception {
        st.getResponse().setResultPacket(new StaxStreamParser().parse(FileUtils.readFileToByteArray(
            new File("src/test/resources/padre-xml/faceted-navigation-urls-smb-selected-encoded.xml")),
            StandardCharsets.UTF_8,
            false));
        
        Assert.assertEquals(0, st.getResponse().getFacets().size());
        
        List<String> selected = new ArrayList<String>();
        selected.add("with%20spaces%20%26%20ampersand");
        st.getQuestion().getSelectedCategoryValues().put("f.By URL|url", selected);

        processor.processOutput(st);
        
        Assert.assertEquals(1, st.getResponse().getFacets().size());
        
        Facet f = st.getResponse().getFacets().get(0);
        Assert.assertEquals("By URL", f.getName());
        Assert.assertEquals(1, f.getCategories().size());
        
        Category c = f.getCategories().get(0);
        Assert.assertEquals("f.By URL|url", c.getQueryStringParamName());
        
        Assert.assertEquals(1, c.getValues().size());
        
        CategoryValue cv = c.getValues().get(0);
        Assert.assertEquals("v", cv.getConstraint());
        Assert.assertEquals(44, cv.getCount() + 0);
        Assert.assertEquals("with spaces & ampersand", cv.getData());
        Assert.assertEquals("with spaces & ampersand", cv.getLabel());
        Assert.assertEquals("f.By+URL%7Curl=share%2FShakespeare%2Fwith%2520spaces%2520%2526%2520ampersand", cv.getQueryStringParam());
        Assert.assertTrue(cv.isSelected());
        
        c = c.getCategories().get(0);
        Assert.assertEquals(3, c.getValues().size());
        
        cv = c.getValues().get(0);
        Assert.assertEquals("v", cv.getConstraint());
        Assert.assertEquals(40, cv.getCount() + 0);
        Assert.assertEquals("with spaces & ampersand/subfolder1", cv.getData());
        Assert.assertEquals("subfolder1", cv.getLabel());
        Assert.assertEquals("f.By+URL%7Curl=share%2FShakespeare%2Fwith%2520spaces%2520%2526%2520ampersand%2Fsubfolder1", cv.getQueryStringParam());
        Assert.assertFalse(cv.isSelected());

        
        cv = c.getValues().get(1);
        Assert.assertEquals("v", cv.getConstraint());
        Assert.assertEquals(3, cv.getCount() + 0);
        Assert.assertEquals("with spaces & ampersand/subfolder2", cv.getData());
        Assert.assertEquals("subfolder2", cv.getLabel());
        Assert.assertEquals("f.By+URL%7Curl=share%2FShakespeare%2Fwith%2520spaces%2520%2526%2520ampersand%2Fsubfolder2", cv.getQueryStringParam());
        Assert.assertFalse(cv.isSelected());

        cv = c.getValues().get(2);
        Assert.assertEquals("v", cv.getConstraint());
        Assert.assertEquals(1, cv.getCount() + 0);
        Assert.assertEquals("with spaces & ampersand/subfolder3", cv.getData());
        Assert.assertEquals("subfolder3", cv.getLabel());
        Assert.assertEquals("f.By+URL%7Curl=share%2FShakespeare%2Fwith%2520spaces%2520%2526%2520ampersand%2Fsubfolder3", cv.getQueryStringParam());
        Assert.assertFalse(cv.isSelected());

    }
    
    
}
