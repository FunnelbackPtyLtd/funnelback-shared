package com.funnelback.publicui.test.search.lifecycle.input.processors;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.common.system.EnvironmentVariableException;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public abstract class FacetedNavigationLegacyAbstractMetadataFieldFillTestMethods {

    private final File SEARCH_HOME = new File("src/test/resources/dummy-search_home");
    protected BothFacetedNavigationInputProcessors processor;
    protected SearchTransaction st;

    public FacetedNavigationLegacyAbstractMetadataFieldFillTestMethods() {
        super();
        
    }

    @Test
    public void testMissingData() throws FileNotFoundException, EnvironmentVariableException {
// TODO remove this?        FacetedNavigation processor = new FacetedNavigation();
        
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
        
        // No faceted navigation config
        question.setCollection(new Collection("dummy", new NoOptionsConfig(SEARCH_HOME, "dummy")));
        st = new SearchTransaction(question, null);
        processor.processInput(st);
        
        // No QP Options
        Collection c = new Collection("dummy", new NoOptionsConfig(SEARCH_HOME, "dummy"));
        c.setFacetedNavigationLiveConfig(new FacetedNavigationConfig(null));
        question.setCollection(c);
        st = new SearchTransaction(question, null);
        processor.processInput(st);
    
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
        Assert.assertEquals("|Z:\"$++ australia $++\"", st.getQuestion().getFacetsQueryConstraints().get(0));
        Assert.assertEquals("|Y:\"$++ nsw $++\"", st.getQuestion().getFacetsQueryConstraints().get(1));
        
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