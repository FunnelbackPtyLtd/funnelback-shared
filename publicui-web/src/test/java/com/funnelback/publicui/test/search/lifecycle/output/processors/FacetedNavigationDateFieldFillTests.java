package com.funnelback.publicui.test.search.lifecycle.output.processors;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.lifecycle.output.processors.FacetedNavigation;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.config.DefaultConfigRepository;
import com.funnelback.publicui.xml.padre.StaxStreamParser;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class FacetedNavigationDateFieldFillTests {

    @Resource(name="localConfigRepository")
    private DefaultConfigRepository configRepository;

    private SearchTransaction st;
    private FacetedNavigation processor;
    
    @Before
    public void before() throws Exception {
        SearchQuestion question = new SearchQuestion();
        question.setCollection(configRepository.getCollection("faceted-navigation-date"));
        
        SearchResponse response = new SearchResponse();
        // Re-use same XML as xpath-fill
        response.setResultPacket(new StaxStreamParser().parse(
            FileUtils.readFileToString(new File("src/test/resources/padre-xml/faceted-navigation-datefieldfill.xml")),
            false));
        st = new SearchTransaction(question, response);
        
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
        sq.setCollection(new Collection("dummy", new NoOptionsConfig("dummy")));
        processor.processOutput(new SearchTransaction(sq, response));
    }
    
    @Test
    public void test() throws OutputProcessorException {
        Assert.assertEquals(0, st.getResponse().getFacets().size());
        processor.processOutput(st);
        
        Assert.assertEquals(2, st.getResponse().getFacets().size());
        
        Facet f = st.getResponse().getFacets().get(0);
        Assert.assertEquals("By date on d,Z,O", f.getName());
        Assert.assertEquals(1, f.getCategories().size());

        // First category: Date on d
        Facet.Category c = f.getCategories().get(0);
        Assert.assertNull(c.getLabel());
        Assert.assertEquals("f.By date on d,Z,O|d", c.getQueryStringParamName());
        Assert.assertEquals(8, c.getValues().size());
        
        Facet.CategoryValue cv = c.getValues().get(0);
        Assert.assertEquals("d", cv.getConstraint());
        Assert.assertEquals(8, cv.getCount());
        Assert.assertEquals("d", cv.getData());
        Assert.assertEquals("Today", cv.getLabel());
        Assert.assertEquals("f.By date on d,Z,O|d=d%3D24Jun2003", cv.getQueryStringParam());
        
        // No sub-categories should be returned since nothing
        // has been selected in the first level category
        Assert.assertEquals(0, c.getCategories().size());

        // Second facet: Date on X
        f = st.getResponse().getFacets().get(1);
        Assert.assertEquals("By date on X", f.getName());
        Assert.assertEquals(1, f.getCategories().size());

        c = f.getCategories().get(0);
        Assert.assertNull(c.getLabel());
        Assert.assertEquals("f.By date on X|X", c.getQueryStringParamName());
        Assert.assertEquals(2, c.getValues().size());

        cv = c.getValues().get(0);
        Assert.assertEquals("X", cv.getConstraint());
        Assert.assertEquals(2137, cv.getCount());
        Assert.assertEquals("X", cv.getData());
        Assert.assertEquals("Past 6 months", cv.getLabel());
        Assert.assertEquals("f.By date on X|X=d%3C25Jun2003%3E26Dec2002", cv.getQueryStringParam());

    }
    
    @Test
    public void testNestedCategorySelection() {
        Assert.assertEquals(0, st.getResponse().getFacets().size());
        
        List<String> selected = new ArrayList<String>();
        selected.add("d=24Jun2003");
        st.getQuestion().getSelectedCategoryValues().put("f.By date on d,Z,O|d", selected);
        processor.processOutput(st);
        
        Assert.assertEquals(2, st.getResponse().getFacets().size());

        Facet f = st.getResponse().getFacets().get(0);
        Assert.assertEquals("By date on d,Z,O", f.getName());
        Assert.assertEquals(1, f.getCategories().size());

        // First category: Date on d
        Facet.Category c = f.getCategories().get(0);
        Assert.assertNull(c.getLabel());
        Assert.assertEquals("f.By date on d,Z,O|d", c.getQueryStringParamName());
        // There's supposed to be 1 value in a real world scenario here because
        // the constraint from the previous click "d=24Jun2003" would have been applied
        Assert.assertEquals(8, c.getValues().size());

        Facet.CategoryValue cv = c.getValues().get(0);
        Assert.assertEquals("d", cv.getConstraint());
        Assert.assertEquals(8, cv.getCount());
        Assert.assertEquals("d", cv.getData());
        Assert.assertEquals("Today", cv.getLabel());
        Assert.assertEquals("f.By date on d,Z,O|d=d%3D24Jun2003", cv.getQueryStringParam());
        
        // Nested category: Date on d + O
        Assert.assertEquals(1, c.getCategories().size());
        Facet.Category subCategory = c.getCategories().get(0);
        Assert.assertNull(subCategory.getLabel());
        Assert.assertEquals("f.By date on d,Z,O|O", subCategory.getQueryStringParamName());
        Assert.assertEquals(2, subCategory.getValues().size());

        cv = subCategory.getValues().get(1);
        Assert.assertEquals("O", cv.getConstraint());
        Assert.assertEquals(12, cv.getCount());
        Assert.assertEquals("O", cv.getData());
        Assert.assertEquals("2005", cv.getLabel());
        Assert.assertEquals("f.By date on d,Z,O|O=O%3D2005", cv.getQueryStringParam());    
    }
}
