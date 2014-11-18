package com.funnelback.publicui.test.search.lifecycle.output.processors;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.publicui.search.lifecycle.output.processors.FacetedNavigation;
import com.funnelback.publicui.search.model.transaction.Facet;
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
            FileUtils.readFileToString(new File("src/test/resources/padre-xml/faceted-navigation-urls-smb.xml")),
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
        Assert.assertEquals(36, c.getValues().size());
        
        Facet.CategoryValue cv = c.getValues().get(0);
        Assert.assertEquals("v", cv.getConstraint());
        Assert.assertEquals(46, cv.getCount());
        Assert.assertEquals("cleopatra", cv.getData());
        Assert.assertEquals("cleopatra", cv.getLabel());
        Assert.assertEquals("f.By+URL%7Curl=share%2FShakespeare%2Fcleopatra", cv.getQueryStringParam());
        
        // No sub-categories should be returned since nothing
        // has been selected in the first level category
        Assert.assertEquals(0, c.getCategories().size());
        
    }
    
    @Test
    public void testCategorySelection() throws Exception {
        st.getResponse().setResultPacket(new StaxStreamParser().parse(FileUtils.readFileToString(
            new File("src/test/resources/padre-xml/faceted-navigation-urls-smb.xml")),
            false));
        
        Assert.assertEquals(0, st.getResponse().getFacets().size());
        
        List<String> selected = new ArrayList<String>();
        selected.add("cleopatra");
        st.getQuestion().getSelectedCategoryValues().put("f.By URL|url", selected);

        processor.processOutput(st);
        
        Assert.assertEquals(1, st.getResponse().getFacets().size());
        
        Facet f = st.getResponse().getFacets().get(0);
        Assert.assertEquals("By URL", f.getName());
        Assert.assertEquals(0, f.getCategories().size());

    }
    
}
