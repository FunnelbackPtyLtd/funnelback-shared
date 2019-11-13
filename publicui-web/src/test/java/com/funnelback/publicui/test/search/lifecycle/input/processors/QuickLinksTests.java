package com.funnelback.publicui.test.search.lifecycle.input.processors;

import com.funnelback.config.configtypes.service.ServiceConfigReadOnly;
import com.funnelback.config.keys.collection.QuickLinkKeys;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.Assert;

import org.junit.Test;

import java.io.File;

import com.funnelback.common.system.EnvironmentVariableException;
import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.QuickLinks;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class QuickLinksTests {

    private QuickLinks processor = new QuickLinks();
    
    private File searchHome = new File("src/test/resources/dummy-search_home");
    
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
    }
    
    @Test
    public void testWithConfig() throws Exception {
        Collection c = new Collection("dummy", new NoOptionsConfig(searchHome, "dummy"));
        c.getConfiguration().setValue(QuickLinkKeys.PREFIX, "enabled");
        c.getConfiguration().setValue(com.funnelback.config.keys.Keys.CollectionKeys.QuickLinkKeys.DEPTH.getKey(), "42");
        c.getConfiguration().setValue(QuickLinkKeys.PREFIX + ".rank", "666");
        
        SearchQuestion question = spy(new SearchQuestion());
        ServiceConfigReadOnly profileConfig = mock(ServiceConfigReadOnly.class);
        when(profileConfig.get(com.funnelback.config.keys.Keys.FrontEndKeys.QueryProcessor.QUERY_PROCESSOR_OPTIONS)).thenReturn("");
        doReturn(profileConfig).when(question).getCurrentProfileConfig();
        
        question.setCollection(c);

        SearchTransaction st = new SearchTransaction(question, null);
        processor.processInput(st);
        Assert.assertEquals(2, st.getQuestion().getDynamicQueryProcessorOptions().size());
        Assert.assertTrue(st.getQuestion().getDynamicQueryProcessorOptions().contains("-QL=42"));
        Assert.assertTrue(st.getQuestion().getDynamicQueryProcessorOptions().contains("-QL_rank=666"));
        
        // Try with conflicting query processor options
        st.getQuestion().getDynamicQueryProcessorOptions().clear();
        when(profileConfig.get(com.funnelback.config.keys.Keys.FrontEndKeys.QueryProcessor.QUERY_PROCESSOR_OPTIONS)).thenReturn("-stem=2 -QL=4 -res=xml -QL_rank=all -something");
        processor.processInput(st);
        Assert.assertEquals(0, st.getQuestion().getDynamicQueryProcessorOptions().size());

        // Try with non-conflicting QP options
        st.getQuestion().getDynamicQueryProcessorOptions().clear();
        when(profileConfig.get(com.funnelback.config.keys.Keys.FrontEndKeys.QueryProcessor.QUERY_PROCESSOR_OPTIONS)).thenReturn("-stem=2 -res=xml -something");
        processor.processInput(st);
        Assert.assertEquals(2, st.getQuestion().getDynamicQueryProcessorOptions().size());
        Assert.assertTrue(st.getQuestion().getDynamicQueryProcessorOptions().contains("-QL=42"));
        Assert.assertTrue(st.getQuestion().getDynamicQueryProcessorOptions().contains("-QL_rank=666"));
    }
    
    @Test
    public void testWithoutConfig() throws Exception {
        Collection c = new Collection("dummy", new NoOptionsConfig(searchHome, "dummy"));
        c.getConfiguration().setValue(QuickLinkKeys.PREFIX, "enabled");
        c.getConfiguration().setValue(QuickLinkKeys.PREFIX + ".rank", "1");
        
        SearchQuestion question = spy(new SearchQuestion());
        ServiceConfigReadOnly profileConfig = mock(ServiceConfigReadOnly.class);
        when(profileConfig.get(com.funnelback.config.keys.Keys.FrontEndKeys.QueryProcessor.QUERY_PROCESSOR_OPTIONS)).thenReturn("");
        doReturn(profileConfig).when(question).getCurrentProfileConfig();
        
        question.setCollection(c);

        SearchTransaction st = new SearchTransaction(question, null);
        processor.processInput(st);
        Assert.assertEquals(2, st.getQuestion().getDynamicQueryProcessorOptions().size());
        Assert.assertTrue(st.getQuestion().getDynamicQueryProcessorOptions().contains("-QL=1"));
        Assert.assertTrue(st.getQuestion().getDynamicQueryProcessorOptions().contains("-QL_rank=1"));
        
        // Try with conflicting query processor options
        st.getQuestion().getDynamicQueryProcessorOptions().clear();
        when(profileConfig.get(com.funnelback.config.keys.Keys.FrontEndKeys.QueryProcessor.QUERY_PROCESSOR_OPTIONS)).thenReturn("-stem=2 -QL=4 -res=xml -QL_rank=all -something");
        processor.processInput(st);
        Assert.assertEquals(0, st.getQuestion().getDynamicQueryProcessorOptions().size());

        // Try with non-conflicting QP options
        st.getQuestion().getDynamicQueryProcessorOptions().clear();
        when(profileConfig.get(com.funnelback.config.keys.Keys.FrontEndKeys.QueryProcessor.QUERY_PROCESSOR_OPTIONS)).thenReturn("-stem=2 -res=xml -something");
        processor.processInput(st);
        Assert.assertEquals(2, st.getQuestion().getDynamicQueryProcessorOptions().size());
        Assert.assertTrue(st.getQuestion().getDynamicQueryProcessorOptions().contains("-QL=1"));
        Assert.assertTrue(st.getQuestion().getDynamicQueryProcessorOptions().contains("-QL_rank=1"));

    }
    
}
