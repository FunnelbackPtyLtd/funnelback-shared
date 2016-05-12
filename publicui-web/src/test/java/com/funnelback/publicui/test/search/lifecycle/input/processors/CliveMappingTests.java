package com.funnelback.publicui.test.search.lifecycle.input.processors;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.common.system.EnvironmentVariableException;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.CliveMapping;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;

public class CliveMappingTests {

    private CliveMapping processor = new CliveMapping();
    private SearchTransaction st;
    
    @Before
    public void before() throws FileNotFoundException, EnvironmentVariableException {
        processor = new CliveMapping();
        Collection c = new Collection("dummy", new NoOptionsConfig(
                new File("src/test/resources/dummy-search_home"), 
            "dummy"));
        SearchQuestion question = new SearchQuestion();
        question.setCollection(c);
        st = new SearchTransaction(question, null);
    }
    

    @Test
    public void testInvalidParameters() {
        try {
            processor.processInput(null);
            processor.processInput(new SearchTransaction(null, null));
            processor.processInput(new SearchTransaction(new SearchQuestion(), null));
            processor.processInput(new SearchTransaction(new SearchQuestion(), null));
            st.getQuestion().setClive(new String[0]);
            processor.processInput(st);
        } catch (Exception e) {
            Assert.fail();
        }
    }
    
    @Test
    public void testNoClive() throws InputProcessorException {
        processor.processInput(st);
        
        Assert.assertNull(st.getQuestion().getClive());
        Assert.assertNull(st.getQuestion().getAdditionalParameters().get(RequestParameters.CLIVE));
    }
    
    @Test
    public void testCliveZeroLengthArray() throws InputProcessorException {
        
        st.getQuestion().setClive(new String[] {});

        processor.processInput(st);
        
        Assert.assertNull(st.getQuestion().getAdditionalParameters().get(RequestParameters.CLIVE));
    }
    
    @Test
    public void testCliveParameterSingleValues() throws InputProcessorException {
        String[] origClive = new String[] {"clive1"};
        
        st.getQuestion().setClive(origClive);
        processor.processInput(st);
        
        String[] clive = st.getQuestion().getAdditionalParameters().get(RequestParameters.CLIVE);
        
        Assert.assertFalse("We should have copied the array, lets avoid shared arrays", 
            origClive == clive);
        
        Assert.assertEquals(origClive[0], clive[0]);
    }
    
    @Test
    public void testCliveParameterMultipleValues() throws InputProcessorException {
        String[] origClive = new String[] {"clive1", "clive2"};
        
        st.getQuestion().setClive(origClive);
        processor.processInput(st);
        
        String[] clive = st.getQuestion().getAdditionalParameters().get(RequestParameters.CLIVE);
        
        Assert.assertFalse("We should have copied the array, lets avoid shared arrays", 
            origClive == clive);
        
        Assert.assertEquals(origClive[0], clive[0]);
        Assert.assertEquals(origClive[1], clive[1]);
       
    }
    
}
