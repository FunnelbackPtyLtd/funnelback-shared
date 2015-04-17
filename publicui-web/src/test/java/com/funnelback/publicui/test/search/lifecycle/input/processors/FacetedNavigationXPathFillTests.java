package com.funnelback.publicui.test.search.lifecycle.input.processors;

import java.io.FileNotFoundException;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.common.system.EnvironmentVariableException;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.lifecycle.input.processors.FacetedNavigation;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.config.DefaultConfigRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class FacetedNavigationXPathFillTests {

    @Resource(name="localConfigRepository")
    private DefaultConfigRepository configRepository;
    
    private FacetedNavigation processor;
    private SearchTransaction st;

    @Before
    public void before() {
        SearchQuestion question = new SearchQuestion();
        question.setCollection(configRepository.getCollection("faceted-navigation-xpathfill"));
        st = new SearchTransaction(question, null);
        
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
        st.getQuestion().getRawInputParameters().put("f.Location|Z", new String[0]);
        processor.processInput(st);
        Assert.assertEquals(0, st.getQuestion().getFacetsQueryConstraints().size());

        st.getQuestion().getRawInputParameters().put("f.Location|Z", new String[] {""});
        processor.processInput(st);
        Assert.assertEquals(0, st.getQuestion().getFacetsQueryConstraints().size());
    }
    
    @Test
    public void testFacetSelected() {
        Assert.assertEquals(0, st.getQuestion().getFacetsQueryConstraints().size());
        Assert.assertNull(st.getQuestion().getFacetsGScopeConstraints());
        
        st.getQuestion().getRawInputParameters().put("f.Location|Z", new String[] {"australia"});
        processor.processInput(st);
        
        Assert.assertNull(st.getQuestion().getFacetsGScopeConstraints());
        Assert.assertEquals(1, st.getQuestion().getFacetsQueryConstraints().size());
        Assert.assertEquals("|Z:\"$++ australia $++\"", st.getQuestion().getFacetsQueryConstraints().get(0));
        
        // Multiple values
        st.getQuestion().getRawInputParameters().clear();
        st.getQuestion().getRawInputParameters().put("f.Location|Z", new String[] {"australia", "new zealand"});
        st.getQuestion().getFacetsQueryConstraints().clear();
        processor.processInput(st);
        
        Assert.assertNull(st.getQuestion().getFacetsGScopeConstraints());
        Assert.assertEquals(1, st.getQuestion().getFacetsQueryConstraints().size());
        Assert.assertTrue(
        		"|[Z:\"$++ new zealand $++\" Z:\"$++ australia $++\"]".equals(st.getQuestion().getFacetsQueryConstraints().get(0))
        		|| "|[Z:\"$++ australia $++\" Z:\"$++ new zealand $++\"]".equals(st.getQuestion().getFacetsQueryConstraints().get(0)));
        
        // Multiple facets
        st.getQuestion().getRawInputParameters().clear();
        st.getQuestion().getRawInputParameters().put("f.Location|Z", new String[] {"australia"});
        st.getQuestion().getRawInputParameters().put("f.Location|Y", new String[] {"nsw"});
        st.getQuestion().getFacetsQueryConstraints().clear();
        processor.processInput(st);
        
        Assert.assertNull(st.getQuestion().getFacetsGScopeConstraints());
        Assert.assertEquals(2, st.getQuestion().getFacetsQueryConstraints().size());
        Assert.assertTrue(
        		st.getQuestion().getFacetsQueryConstraints().contains("|Z:\"$++ australia $++\"")
        		|| st.getQuestion().getFacetsQueryConstraints().contains("|Y:\"$++ nsw $++\""));
        
        // Multiple values + multiple facets
        st.getQuestion().getRawInputParameters().clear();
        st.getQuestion().getRawInputParameters().put("f.Location|Z", new String[] {"australia", "new zealand"});
        st.getQuestion().getRawInputParameters().put("f.Location|Y", new String[] {"nsw", "tas"});
        st.getQuestion().getFacetsQueryConstraints().clear();
        processor.processInput(st);
        
        Assert.assertNull(st.getQuestion().getFacetsGScopeConstraints());
        Assert.assertEquals(2, st.getQuestion().getFacetsQueryConstraints().size());
        Assert.assertTrue(
        		st.getQuestion().getFacetsQueryConstraints().contains("|[Z:\"$++ new zealand $++\" Z:\"$++ australia $++\"]")
        		|| st.getQuestion().getFacetsQueryConstraints().contains("|[Z:\"$++ australia $++\" Z:\"$++ new zealand $++\"]"));
        Assert.assertTrue(
        		st.getQuestion().getFacetsQueryConstraints().contains("|[Y:\"$++ nsw $++\" Y:\"$++ tas $++\"]")
        		|| st.getQuestion().getFacetsQueryConstraints().contains("|[Y:\"$++ tas $++\" Y:\"$++ nsw $++\"]")); 
    }
    

    @Test
    public void testSameName() {
        Assert.assertEquals(0, st.getQuestion().getFacetsQueryConstraints().size());
        Assert.assertNull(st.getQuestion().getFacetsGScopeConstraints());
        
        st.getQuestion().getRawInputParameters().put("f.Location|O", new String[] {"australia"});
        processor.processInput(st);
        
        Assert.assertNull(st.getQuestion().getFacetsGScopeConstraints());
        Assert.assertEquals(1, st.getQuestion().getFacetsQueryConstraints().size());
        Assert.assertEquals("|O:\"$++ australia $++\"", st.getQuestion().getFacetsQueryConstraints().get(0));
    }

}
