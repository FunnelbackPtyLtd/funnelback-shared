package com.funnelback.publicui.search.lifecycle.output.processors;

import static com.funnelback.publicui.search.model.collection.facetednavigation.FacetExtraSearchNames.SEARCH_FOR_UNSCOPED_VALUES;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.config.configtypes.service.ServiceConfig;
import com.funnelback.config.data.service.InMemoryServiceConfig;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.google.common.collect.ImmutableMap;
public class RemoveFacetedNavigationExtraSearchesTest {

    @Test
    public void testRemovingFacetExtraSearches() throws Exception {
        SearchQuestion searchQuestion = spy(new SearchQuestion());
        
        ServiceConfig serviceConfig = new InMemoryServiceConfig(new HashMap<>());
        
        doReturn(serviceConfig).when(searchQuestion).getCurrentProfileConfig();
        
        SearchResponse response = new SearchResponse();
        SearchTransaction st = new SearchTransaction(searchQuestion, response);
        
        st.getExtraSearches().put(SEARCH_FOR_UNSCOPED_VALUES, new SearchTransaction());
        st.getExtraSearches().put("other", new SearchTransaction());
        
        new RemoveFacetedNavigationExtraSearches().processOutput(st);
        
        Assert.assertEquals(1, st.getExtraSearches().size());
    }
    
    @Test
    public void testKeepingFacetExtraSearches() throws Exception {
        SearchQuestion searchQuestion = spy(new SearchQuestion());
        
        ServiceConfig serviceConfig = new InMemoryServiceConfig(ImmutableMap.of("ui.modern.remove-internal-extra-searches", "false"));
        
        doReturn(serviceConfig).when(searchQuestion).getCurrentProfileConfig();
        
        SearchResponse response = new SearchResponse();
        SearchTransaction st = new SearchTransaction(searchQuestion, response);
        
        st.getExtraSearches().put(SEARCH_FOR_UNSCOPED_VALUES, new SearchTransaction());
        st.getExtraSearches().put("other", new SearchTransaction());
        
        new RemoveFacetedNavigationExtraSearches().processOutput(st);
        
        Assert.assertEquals(2, st.getExtraSearches().size());
    }
}
