package com.funnelback.publicui.accessibilityauditor.lifecycle.output.processors;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.accessibilityauditor.lifecycle.input.processors.ReMapRMCFOptionInputProcessor;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class ReMapRMCFOptionInputProcessorTest {

    @Test
    public void test() throws Exception {
        // FunAAFormat -> should be re-mapped
        // FunAASetOfFailingTechniques -> should be re-mapped
        // FunAADomain -> should be ignored
        
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);
        st.getQuestion().getDynamicQueryProcessorOptions().add("-opt");
        st.getQuestion().getDynamicQueryProcessorOptions().add("-rmcf=");
        st.getQuestion().getDynamicQueryProcessorOptions().add("-rmcf=[]");
        st.getQuestion().getDynamicQueryProcessorOptions().add("-rmcf=[FunAADomain]");
        st.getQuestion().getDynamicQueryProcessorOptions().add("-rmcf=[FunAASetOfFailingTechniques,bar,FunAAFormater,FunAAFormat]");
        
        new ReMapRMCFOptionInputProcessor().processAccessibilityAuditorTransaction(st);
        
        List<String> dynamicQPOpts = st.getQuestion().getDynamicQueryProcessorOptions();
        
        Assert.assertEquals("Expected non rmcf option to be left alone", "-opt", dynamicQPOpts.get(0));
        Assert.assertEquals("Expected -rmcf opt with no vales to be left alone", "-rmcf=", dynamicQPOpts.get(1));
        Assert.assertEquals("Expected -rmcf opt with no vales to be left alone", "-rmcf=[]", dynamicQPOpts.get(2));
        Assert.assertEquals("Expected -rmcf opt with no remapable values to be left alone",
            "-rmcf=[FunAADomain]", dynamicQPOpts.get(3));
        Assert.assertEquals("Expected -rmcf with remapable options to be alterd",
            "-rmcf=[bar,FunAAFormater]", dynamicQPOpts.get(4));
        
        Assert.assertEquals("Expected a contIndexedTerms option to be added with the values that we remapped", 
            "-countIndexedTerms=[FunAASetOfFailingTechniques,FunAAFormat]", dynamicQPOpts.get(5));
        
    }
    
    @Test
    public void testWithExistingCountIndexedTermsOption() throws Exception {
        // FunAAFormat -> should be re-mapped
        // FunAASetOfFailingTechniques -> should be re-mapped
        // FunAADomain -> should be ignored
        
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);
        st.getQuestion().getDynamicQueryProcessorOptions().add("-rmcf=[FunAASetOfFailingTechniques,bar,FunAAFormater,FunAAFormat]");
        st.getQuestion().getDynamicQueryProcessorOptions().add("-countIndexedTerms=[foo,bar]");
        st.getQuestion().getDynamicQueryProcessorOptions().add("-countIndexedTerms=[foo,bar]");
        
        new ReMapRMCFOptionInputProcessor().processAccessibilityAuditorTransaction(st);
        
        List<String> dynamicQPOpts = st.getQuestion().getDynamicQueryProcessorOptions();
        
        
        Assert.assertEquals("Expected each contIndexedTerms option to be updated with the values that we remapped", 
            "-countIndexedTerms=[foo,bar,FunAASetOfFailingTechniques,FunAAFormat]", dynamicQPOpts.get(2));
        
        Assert.assertEquals("Expected each contIndexedTerms option to be updated with the values that we remapped", 
            "-countIndexedTerms=[foo,bar,FunAASetOfFailingTechniques,FunAAFormat]", dynamicQPOpts.get(2));
        
        Assert.assertEquals("Should not have added a extra countIndexedTerms option", 3, dynamicQPOpts.size());
        
    }
    
    @Test
    public void existingNoValueCountIndexTerms() throws Exception {
        
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);
        st.getQuestion().getDynamicQueryProcessorOptions().add("-rmcf=[FunAASetOfFailingTechniques,bar,FunAAFormater,FunAAFormat]");
        st.getQuestion().getDynamicQueryProcessorOptions().add("-countIndexedTerms=");
        
        new ReMapRMCFOptionInputProcessor().processAccessibilityAuditorTransaction(st);
        
        List<String> dynamicQPOpts = st.getQuestion().getDynamicQueryProcessorOptions();
        
        
        Assert.assertEquals( 
            "-countIndexedTerms=[FunAASetOfFailingTechniques,FunAAFormat]", dynamicQPOpts.get(1));
        
    }
    
    @Test
    public void existingEmptyCountIndexTerms() throws Exception {
        
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);
        st.getQuestion().getDynamicQueryProcessorOptions().add("-rmcf=[FunAASetOfFailingTechniques,bar,FunAAFormater,FunAAFormat]");
        st.getQuestion().getDynamicQueryProcessorOptions().add("-countIndexedTerms=[]");
        
        new ReMapRMCFOptionInputProcessor().processAccessibilityAuditorTransaction(st);
        
        List<String> dynamicQPOpts = st.getQuestion().getDynamicQueryProcessorOptions();
        
        
        Assert.assertEquals( 
            "-countIndexedTerms=[FunAASetOfFailingTechniques,FunAAFormat]", dynamicQPOpts.get(1));
        
    }
}
