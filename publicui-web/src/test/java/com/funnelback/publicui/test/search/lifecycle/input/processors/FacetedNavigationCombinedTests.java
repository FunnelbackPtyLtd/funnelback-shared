package com.funnelback.publicui.test.search.lifecycle.input.processors;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.config.DefaultConfigRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class FacetedNavigationCombinedTests {

    @Resource(name="localConfigRepository")
    private DefaultConfigRepository configRepository;
    
    private BothFacetedNavigationInputProcessors processor;
    private SearchTransaction st;

    @Before
    public void before() {
        SearchQuestion question = new SearchQuestion();
        question.setCollection(configRepository.getCollection("faceted-navigation-combined"));
        st = new SearchTransaction(question, null);
        
        processor = new BothFacetedNavigationInputProcessors();
        processor.switchAllFacetConfigToSelectionAnd(st);
    }
    
    @Test
    public void testFacetSelected() {
        Assert.assertEquals(0, st.getQuestion().getFacetsQueryConstraints().size());
        Assert.assertNull(st.getQuestion().getFacetsGScopeConstraints());
        
        st.getQuestion().getRawInputParameters().put("f.By URL|url", new String[] {"Shakespeare/cleopatra"});
        // This next line does not make sense when would this happen?
        st.getQuestion().getRawInputParameters().put("f.By Date|d", new String[] {"1500-01-01", "1600-01-01"});
        st.getQuestion().getRawInputParameters().put("f.By Query|41", new String[] {"King"});
        st.getQuestion().getRawInputParameters().put("f.By Play|10", new String[] {"Coriolanus"});
        st.getQuestion().getRawInputParameters().put("f.By Play|1", new String[] {"Henry IV"});
        processor.processInput(st);
        
        Assert.assertNotNull(st.getQuestion().getFacetsGScopeConstraints());
        
        // Note that 1,10,41++ is also valid.
        // the current implementation happens to do something like 1,10+41+
        
        Assert.assertTrue("We expect all three gscopes to be logical AND (note this is reverse polish) "
             + st.getQuestion().getFacetsGScopeConstraints(), 
            st.getQuestion().getFacetsGScopeConstraints().endsWith("++")||
            st.getQuestion().getFacetsGScopeConstraints().equals("1,10+41+"));
        
        //We will check we set the correct constrains by replace '+' and ',' with space and spluting on space.
        List<String> gscopesConstrains = Arrays.asList(
            st.getQuestion().getFacetsGScopeConstraints().replace("+", " ").replace(",", " ").split(" "));
        Assert.assertTrue(gscopesConstrains.contains("1"));
        Assert.assertTrue(gscopesConstrains.contains("10"));
        Assert.assertTrue(gscopesConstrains.contains("41"));
        Assert.assertEquals(2, st.getQuestion().getFacetsQueryConstraints().size());
        
        Assert.assertTrue(
            "we should see the 1600 date coinstrain.",
            st.getQuestion().getFacetsQueryConstraints().contains("|d:\"$++ 1600-01-01 $++\""));
        
        Assert.assertTrue(
            "Should see the 1500 date constraint.",
            st.getQuestion().getFacetsQueryConstraints().contains("|d:\"$++ 1500-01-01 $++\""));
    }
    
    @Test
    public void testMissingParameter() {
        Assert.assertEquals(0, st.getQuestion().getFacetsQueryConstraints().size());
        Assert.assertNull(st.getQuestion().getFacetsGScopeConstraints());
        
        st.getQuestion().getRawInputParameters().put("f.By URL|", new String[] {"Shakespeare/cleopatra"});
        st.getQuestion().getRawInputParameters().put("f.By URL", new String[] {"Shakespeare/cleopatra"});
        st.getQuestion().getRawInputParameters().put("f.By Date|", new String[] {"1500-01-01", "1600-01-01"});
        st.getQuestion().getRawInputParameters().put("f.By Date", new String[] {"1500-01-01", "1600-01-01"});
        st.getQuestion().getRawInputParameters().put("f.By Query|", new String[] {"King"});
        st.getQuestion().getRawInputParameters().put("f.By Query", new String[] {"King"});
        st.getQuestion().getRawInputParameters().put("f.By Play|", new String[] {"Coriolanus"});
        st.getQuestion().getRawInputParameters().put("f.By Play", new String[] {"Henry IV"});
        processor.processInput(st);
        
        Assert.assertNull(st.getQuestion().getFacetsGScopeConstraints());       
    }
    
    
}
