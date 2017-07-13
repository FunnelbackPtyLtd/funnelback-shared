package com.funnelback.publicui.test.search.lifecycle.input.processors;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.publicui.search.lifecycle.input.processors.FacetedNavigation;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.config.DefaultConfigRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class FacetedNavigationDateTests {

    @Resource(name="localConfigRepository")
    private DefaultConfigRepository configRepository;
    
    private FacetedNavigation processor;
    private SearchTransaction st;

    @Before
    public void before() {
        SearchQuestion question = new SearchQuestion();
        question.setCollection(configRepository.getCollection("faceted-navigation-date"));
        st = new SearchTransaction(question, null);
        
        processor = new FacetedNavigation();
    }
    
    @Test
    public void testEmpty() {
        st.getQuestion().getRawInputParameters().put("f.By date on d,Z,O|d", new String[0]);
        processor.processInput(st);
        Assert.assertEquals(0, st.getQuestion().getFacetsQueryConstraints().size());

        st.getQuestion().getRawInputParameters().put("f.By date on d,Z,O|d", new String[] {""});
        processor.processInput(st);
        Assert.assertEquals(0, st.getQuestion().getFacetsQueryConstraints().size());
    }
    
    @Test
    public void testFacetSelected() {
        Assert.assertEquals(0, st.getQuestion().getFacetsQueryConstraints().size());
        Assert.assertNull(st.getQuestion().getFacetsGScopeConstraints());
        
        st.getQuestion().getRawInputParameters().put("f.By date on d,Z,O|d", new String[] {"d=2003"});
        processor.processInput(st);
        
        Assert.assertNull(st.getQuestion().getFacetsGScopeConstraints());
        Assert.assertEquals(1, st.getQuestion().getFacetsQueryConstraints().size());
        Assert.assertEquals("|d=2003", st.getQuestion().getFacetsQueryConstraints().get(0));

        st.getQuestion().getRawInputParameters().clear();
        st.getQuestion().getRawInputParameters().put("f.By date on d,Z,O|d", new String[] {"d>2003", "d<2004"});
        st.getQuestion().getFacetsQueryConstraints().clear();
        processor.processInput(st);
        
        Assert.assertNull(st.getQuestion().getFacetsGScopeConstraints());
        Assert.assertEquals(2, st.getQuestion().getFacetsQueryConstraints().size());
        
        Assert.assertTrue("We are looking for \"|d>2003\"", 
            st.getQuestion().getFacetsQueryConstraints().contains("|d>2003"));
        
        Assert.assertTrue("We are looking for \"|d<2004\"", 
            st.getQuestion().getFacetsQueryConstraints().contains("|d<2004"));

        st.getQuestion().getRawInputParameters().clear();
        st.getQuestion().getRawInputParameters().put("f.By date on d,Z,O|d", new String[] {"d>2003"});
        st.getQuestion().getRawInputParameters().put("f.By date on d,Z,O|Z", new String[] {"Z>1Jan2005"});
        st.getQuestion().getFacetsQueryConstraints().clear();
        processor.processInput(st);
        
        Assert.assertNull(st.getQuestion().getFacetsGScopeConstraints());
        Assert.assertEquals(2, st.getQuestion().getFacetsQueryConstraints().size());
        Assert.assertTrue(st.getQuestion().getFacetsQueryConstraints().contains("|d>2003"));
        Assert.assertTrue(st.getQuestion().getFacetsQueryConstraints().contains("|Z>1Jan2005"));

        st.getQuestion().getRawInputParameters().clear();
        st.getQuestion().getRawInputParameters().put("f.By date on d,Z,O|d", new String[] {"d>2003", "d<2004"});
        st.getQuestion().getRawInputParameters().put("f.By date on d,Z,O|Z", new String[] {"Z>1Jan2005", "Z<12Mar2010"});
        st.getQuestion().getFacetsQueryConstraints().clear();
        processor.processInput(st);
        
        Assert.assertNull(st.getQuestion().getFacetsGScopeConstraints());
        Assert.assertEquals(4, st.getQuestion().getFacetsQueryConstraints().size());
        
        
        Assert.assertTrue("We are looking for \"|d>2003\"", 
            st.getQuestion().getFacetsQueryConstraints().contains("|d>2003"));
        
        Assert.assertTrue("We are looking for \"|d<2004\"", 
            st.getQuestion().getFacetsQueryConstraints().contains("|d<2004"));
        
        Assert.assertTrue("We are looking for \"|d>Z>1Jan2005\"", 
            st.getQuestion().getFacetsQueryConstraints().contains("|Z>1Jan2005"));
        
        Assert.assertTrue("We are looking for \"|Z<12Mar2010\"", 
            st.getQuestion().getFacetsQueryConstraints().contains("|Z<12Mar2010"));
       
    }
    
    @Test
    public void testSameName() {
        Assert.assertEquals(0, st.getQuestion().getFacetsQueryConstraints().size());
        Assert.assertNull(st.getQuestion().getFacetsGScopeConstraints());
        
        st.getQuestion().getRawInputParameters().put("f.By date on d,Z,O|Z", new String[] {"Z<1Jan2003"});
        processor.processInput(st);
        
        Assert.assertNull(st.getQuestion().getFacetsGScopeConstraints());
        Assert.assertEquals(1, st.getQuestion().getFacetsQueryConstraints().size());
        Assert.assertEquals("|Z<1Jan2003", st.getQuestion().getFacetsQueryConstraints().get(0));
    }
    
    
}
