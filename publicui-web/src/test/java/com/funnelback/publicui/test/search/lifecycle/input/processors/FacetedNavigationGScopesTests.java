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
public class FacetedNavigationGScopesTests {

    @Resource(name="localConfigRepository")
    private DefaultConfigRepository configRepository;
    
    private BothFacetedNavigationInputProcessors processor;
    private SearchTransaction st;

    @Before
    public void before() {
        SearchQuestion question = new SearchQuestion();
        question.setCollection(configRepository.getCollection("faceted-navigation-gscopes"));
        st = new SearchTransaction(question, null);
        
        processor = new BothFacetedNavigationInputProcessors();
        processor.switchAllFacetConfigToSelectionAnd(st);
    }
    
    @Test
    public void testEmpty() {
        st.getQuestion().getRawInputParameters().put("f.By Story|1", new String[0]);
        processor.processInput(st);
        Assert.assertNull(st.getQuestion().getFacetsGScopeConstraints());

        st.getQuestion().getRawInputParameters().put("f.By Story|1", new String[] {""});
        processor.processInput(st);
        Assert.assertNull(st.getQuestion().getFacetsGScopeConstraints());
    }
    
    @Test
    public void testFacetSelected() {
        Assert.assertEquals(0, st.getQuestion().getFacetsQueryConstraints().size());
        Assert.assertNull(st.getQuestion().getFacetsGScopeConstraints());
        
        st.getQuestion().getRawInputParameters().put("f.By Story|1", new String[] {"Henry IV"});
        processor.processInput(st);
        
        Assert.assertEquals(0, st.getQuestion().getFacetsQueryConstraints().size());
        Assert.assertEquals("1", st.getQuestion().getFacetsGScopeConstraints());
        
        // Multiple categories
        st.getQuestion().getRawInputParameters().clear();
        st.getQuestion().getRawInputParameters().put("f.By Story|1", new String[] {"Henry IV"});
        st.getQuestion().getRawInputParameters().put("f.By Story|10", new String[] {"Coriolanus"});
        st.getQuestion().getFacetsQueryConstraints().clear();
        st.getQuestion().setFacetsGScopeConstraints(null);
        processor.processInput(st);
        
        Assert.assertEquals(0, st.getQuestion().getFacetsQueryConstraints().size());
        // FIXME: FUN-4480 This should be 10,1| here because both values are part of the same facet
        Assert.assertTrue("10,1+".equals(st.getQuestion().getFacetsGScopeConstraints()) || "1,10+".equals(st.getQuestion().getFacetsGScopeConstraints()));        
    }
    
    @Test
    public void testSameName() {
        Assert.assertEquals(0, st.getQuestion().getFacetsQueryConstraints().size());
        Assert.assertNull(st.getQuestion().getFacetsGScopeConstraints());
        
        st.getQuestion().getRawInputParameters().put("f.By Story|16", new String[] {"Henry IV"});
        processor.processInput(st);
        
        Assert.assertEquals(0, st.getQuestion().getFacetsQueryConstraints().size());
        Assert.assertEquals("16", st.getQuestion().getFacetsGScopeConstraints());
        
        // Multiple categories
        st.getQuestion().getRawInputParameters().clear();
        st.getQuestion().getRawInputParameters().put("f.By Story|16", new String[] {"Henry IV"});
        st.getQuestion().getRawInputParameters().put("f.By Story|10", new String[] {"Coriolanus"});
        st.getQuestion().getFacetsQueryConstraints().clear();
        st.getQuestion().setFacetsGScopeConstraints(null);
        processor.processInput(st);
        
        Assert.assertEquals(0, st.getQuestion().getFacetsQueryConstraints().size());
        Assert.assertTrue("Wa want '10,16+' or '16,10+' (order is not important) yet we got: " 
                + st.getQuestion().getFacetsGScopeConstraints(), 
                st.getQuestion().getFacetsGScopeConstraints().equals("10,16+")
                || st.getQuestion().getFacetsGScopeConstraints().equals("16,10+"));                
    }
    
    
}
