package com.funnelback.publicui.test.search.lifecycle.output.processors;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.lifecycle.output.processors.DateFacetSort;
import com.funnelback.publicui.search.lifecycle.output.processors.FacetedNavigation;
import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.config.DefaultConfigRepository;
import com.funnelback.publicui.xml.padre.StaxStreamParser;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class DateFacetSortTests {

    @Resource(name="localConfigRepository")
    private DefaultConfigRepository configRepository;

    private SearchTransaction st;
    private DateFacetSort processor;

    @Before
    public void before() throws Exception {
        SearchQuestion question = new SearchQuestion();
        question.setCollection(configRepository.getCollection("faceted-navigation-date"));
        
        SearchResponse response = new SearchResponse();
        // Re-use same XML as xpath-fill
        response.setResultPacket(new StaxStreamParser().parse(
            FileUtils.readFileToByteArray(new File("src/test/resources/padre-xml/faceted-navigation-datefieldfill.xml")), 
            StandardCharsets.UTF_8,
            false));
        st = new SearchTransaction(question, response);
        
        new FacetedNavigation().processOutput(st);
        processor = new DateFacetSort();
    }
    
    @Test
    public void testSortByCount() throws OutputProcessorException {
        configRepository.getCollection("faceted-navigation-date").getConfiguration()
            .setValue(Keys.FacetedNavigation.DATE_SORT_MODE, "count");

        processor.processOutput(st);
        
        Facet f = st.getResponse().getFacets().get(0);
        List<CategoryValue> vals = f.getCategories().get(0).getValues();

        Assert.assertEquals(8, vals.size());
        Assert.assertEquals(vals.get(0).getLabel(), "Today");
        Assert.assertEquals(vals.get(1).getLabel(), "Past 6 months");
        Assert.assertEquals(vals.get(2).getLabel(), "Past week");
        Assert.assertEquals(vals.get(3).getLabel(), "Past month");
        Assert.assertEquals(vals.get(4).getLabel(), "Past year");
        Assert.assertEquals(vals.get(5).getLabel(), "Yesterday");
        Assert.assertEquals(vals.get(6).getLabel(), "2003");
        Assert.assertEquals(vals.get(7).getLabel(), "Past 3 months");
        
        f = st.getResponse().getFacets().get(1);
        vals = f.getCategories().get(0).getValues();
        Assert.assertEquals(vals.get(0).getLabel(), "Past 6 months");
        Assert.assertEquals(vals.get(1).getLabel(), "Today");
    }

    @Test
    public void testSortByDateDesc() throws OutputProcessorException {
        configRepository.getCollection("faceted-navigation-date").getConfiguration()
            .setValue(Keys.FacetedNavigation.DATE_SORT_MODE, "ddate");

        processor.processOutput(st);
        
        Facet f = st.getResponse().getFacets().get(0);
        List<CategoryValue> vals = f.getCategories().get(0).getValues();

        Assert.assertEquals(8, vals.size());
        Assert.assertEquals(vals.get(0).getLabel(), "Today");
        Assert.assertEquals(vals.get(1).getLabel(), "Yesterday");
        Assert.assertEquals(vals.get(2).getLabel(), "Past week");
        Assert.assertEquals(vals.get(3).getLabel(), "Past month");
        Assert.assertEquals(vals.get(4).getLabel(), "Past 3 months");
        Assert.assertEquals(vals.get(5).getLabel(), "Past 6 months");
        Assert.assertEquals(vals.get(6).getLabel(), "Past year");        
        Assert.assertEquals(vals.get(7).getLabel(), "2003");
        
        
        f = st.getResponse().getFacets().get(1);
        vals = f.getCategories().get(0).getValues();
        Assert.assertEquals(vals.get(0).getLabel(), "Today");
        Assert.assertEquals(vals.get(1).getLabel(), "Past 6 months");
    }

    @Test
    public void testSortByDateAsc() throws OutputProcessorException {
        configRepository.getCollection("faceted-navigation-date").getConfiguration()
            .setValue(Keys.FacetedNavigation.DATE_SORT_MODE, "adate");

        processor.processOutput(st);
        
        Facet f = st.getResponse().getFacets().get(0);
        List<CategoryValue> vals = f.getCategories().get(0).getValues();

        Assert.assertEquals(8, vals.size());
        Assert.assertEquals(vals.get(7).getLabel(), "Today");
        Assert.assertEquals(vals.get(6).getLabel(), "Yesterday");
        Assert.assertEquals(vals.get(5).getLabel(), "Past week");
        Assert.assertEquals(vals.get(4).getLabel(), "Past month");
        Assert.assertEquals(vals.get(3).getLabel(), "Past 3 months");
        Assert.assertEquals(vals.get(2).getLabel(), "Past 6 months");
        Assert.assertEquals(vals.get(1).getLabel(), "Past year");        
        Assert.assertEquals(vals.get(0).getLabel(), "2003");
        
        
        f = st.getResponse().getFacets().get(1);
        vals = f.getCategories().get(0).getValues();
        Assert.assertEquals(vals.get(1).getLabel(), "Today");
        Assert.assertEquals(vals.get(0).getLabel(), "Past 6 months");
    }

}
