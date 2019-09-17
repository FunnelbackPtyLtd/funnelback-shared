package com.funnelback.publicui.test.search.lifecycle.input.processors;

import java.util.HashMap;

import com.funnelback.config.keys.collection.QuickLinkKeys;
import org.junit.Assert;

import org.junit.Test;

import com.funnelback.common.system.EnvironmentVariableException;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.QuickLinks;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class QuickLinksTests {

    private QuickLinks processor = new QuickLinks();
    
    @Test
    public void testMissingData() throws InputProcessorException {
        // No transaction
        processor.processInput(null);
        
        // No question
        processor.processInput(new SearchTransaction(null, null));
        
        // No collection
        SearchQuestion question = new SearchQuestion();
        SearchTransaction st = new SearchTransaction(question, null);
        processor.processInput(st);
        Assert.assertEquals(0, st.getQuestion().getDynamicQueryProcessorOptions().size());
        
        // No quicklinks config
        question.setCollection(new Collection("dummy", null));
        processor.processInput(new SearchTransaction(question, null));
        Assert.assertEquals(0, st.getQuestion().getDynamicQueryProcessorOptions().size());

        // Empty quicklinks config
        Collection c = new Collection("dummy", null);
        c.setQuickLinksConfiguration(new HashMap<String, String>());
        question.setCollection(c);
        processor.processInput(new SearchTransaction(question, null));
        Assert.assertEquals(0, st.getQuestion().getDynamicQueryProcessorOptions().size());

        // quicklinks disabled
        c.getQuickLinksConfiguration().put(QuickLinkKeys.PREFIX, "disabled");
        question.setCollection(c);
        processor.processInput(new SearchTransaction(question, null));
        Assert.assertEquals(0, st.getQuestion().getDynamicQueryProcessorOptions().size());

    }
    
    @Test
    public void testWithConfig() throws InputProcessorException, EnvironmentVariableException {
        HashMap<String, String> qlConfig = new HashMap<String, String>();
        qlConfig.put(QuickLinkKeys.PREFIX, "enabled");
        qlConfig.put(com.funnelback.config.keys.Keys.CollectionKeys.QuickLinkKeys.DEPTH.getKey(), "42");
        qlConfig.put(QuickLinkKeys.PREFIX + ".rank", "666");
        
        Collection c = new Collection("dummy", new NoOptionsConfig("dummy"));
        c.setQuickLinksConfiguration(qlConfig);
        
        SearchQuestion question = new SearchQuestion();
        question.setCollection(c);

        SearchTransaction st = new SearchTransaction(question, null);
        processor.processInput(st);
        Assert.assertEquals(2, st.getQuestion().getDynamicQueryProcessorOptions().size());
        Assert.assertTrue(st.getQuestion().getDynamicQueryProcessorOptions().contains("-QL=42"));
        Assert.assertTrue(st.getQuestion().getDynamicQueryProcessorOptions().contains("-QL_rank=666"));
        
        // Try with conflicting query processor options
        st.getQuestion().getDynamicQueryProcessorOptions().clear();
        c.getConfiguration().setValue(com.funnelback.common.config.Keys.QUERY_PROCESSOR_OPTIONS, "-stem=2 -QL=4 -res=xml -QL_rank=all -something");
        processor.processInput(st);
        Assert.assertEquals(0, st.getQuestion().getDynamicQueryProcessorOptions().size());

        // Try with non-conflicting QP options
        st.getQuestion().getDynamicQueryProcessorOptions().clear();
        c.getConfiguration().setValue(com.funnelback.common.config.Keys.QUERY_PROCESSOR_OPTIONS, "-stem=2 -res=xml -something");
        processor.processInput(st);
        Assert.assertEquals(2, st.getQuestion().getDynamicQueryProcessorOptions().size());
        Assert.assertTrue(st.getQuestion().getDynamicQueryProcessorOptions().contains("-QL=42"));
        Assert.assertTrue(st.getQuestion().getDynamicQueryProcessorOptions().contains("-QL_rank=666"));

        
    }
    
    @Test
    public void testWithoutConfig() throws InputProcessorException, EnvironmentVariableException {
        HashMap<String, String> qlConfig = new HashMap<String, String>();
        qlConfig.put(QuickLinkKeys.PREFIX, "enabled");
        qlConfig.put(QuickLinkKeys.PREFIX + ".rank", "1");
        
        Collection c = new Collection("dummy", new NoOptionsConfig("dummy"));
        c.setQuickLinksConfiguration(qlConfig);
        
        SearchQuestion question = new SearchQuestion();
        question.setCollection(c);

        SearchTransaction st = new SearchTransaction(question, null);
        processor.processInput(st);
        Assert.assertEquals(2, st.getQuestion().getDynamicQueryProcessorOptions().size());
        Assert.assertTrue(st.getQuestion().getDynamicQueryProcessorOptions().contains("-QL=1"));
        Assert.assertTrue(st.getQuestion().getDynamicQueryProcessorOptions().contains("-QL_rank=1"));
        
        // Try with conflicting query processor options
        st.getQuestion().getDynamicQueryProcessorOptions().clear();
        c.getConfiguration().setValue(com.funnelback.common.config.Keys.QUERY_PROCESSOR_OPTIONS, "-stem=2 -QL=4 -res=xml -QL_rank=all -something");
        processor.processInput(st);
        Assert.assertEquals(0, st.getQuestion().getDynamicQueryProcessorOptions().size());

        // Try with non-conflicting QP options
        st.getQuestion().getDynamicQueryProcessorOptions().clear();
        c.getConfiguration().setValue(com.funnelback.common.config.Keys.QUERY_PROCESSOR_OPTIONS, "-stem=2 -res=xml -something");
        processor.processInput(st);
        Assert.assertEquals(2, st.getQuestion().getDynamicQueryProcessorOptions().size());
        Assert.assertTrue(st.getQuestion().getDynamicQueryProcessorOptions().contains("-QL=1"));
        Assert.assertTrue(st.getQuestion().getDynamicQueryProcessorOptions().contains("-QL_rank=1"));

    }
    
}
