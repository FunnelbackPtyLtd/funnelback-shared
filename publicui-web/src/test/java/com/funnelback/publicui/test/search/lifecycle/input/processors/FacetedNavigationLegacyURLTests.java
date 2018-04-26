package com.funnelback.publicui.test.search.lifecycle.input.processors;

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
public class FacetedNavigationLegacyURLTests {

    @Resource(name="localConfigRepository")
    private DefaultConfigRepository configRepository;
    
    private BothFacetedNavigationInputProcessors processor;
    private SearchTransaction st;

    @Before
    public void before() {
        SearchQuestion question = new SearchQuestion();
        question.setCollection(configRepository.getCollection("faceted-navigation-urls"));
        st = new SearchTransaction(question, null);
        
        processor = new BothFacetedNavigationInputProcessors();
    }
    
    @Test
    public void testEmpty() {
        st.getQuestion().getRawInputParameters().put("f.By URL|url", new String[0]);
        processor.processInput(st);
        Assert.assertEquals(0, st.getQuestion().getFacetsQueryConstraints().size());

        st.getQuestion().getRawInputParameters().put("f.By URL|url", new String[] {""});
        processor.processInput(st);
        Assert.assertEquals(0, st.getQuestion().getFacetsQueryConstraints().size());
    }
    
    @Test
    public void testFacetSelected() {
        Assert.assertEquals(0, st.getQuestion().getFacetsQueryConstraints().size());
        Assert.assertNull(st.getQuestion().getFacetsGScopeConstraints());
        
        st.getQuestion().getRawInputParameters().put("f.By URL|url", new String[] {"Shakespeare/cleopatra"});
        processor.processInput(st);
        
        Assert.assertNull(st.getQuestion().getFacetsGScopeConstraints());
        Assert.assertEquals("URL fill scoping QP options are set by calling getQueryProcessorOptions()",
            0, st.getQuestion().getFacetsQueryConstraints().size());
    }
    
    
}
