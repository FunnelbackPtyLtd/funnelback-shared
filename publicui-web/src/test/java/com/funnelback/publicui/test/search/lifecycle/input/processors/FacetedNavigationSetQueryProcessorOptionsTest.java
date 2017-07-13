package com.funnelback.publicui.test.search.lifecycle.input.processors;

import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.qpo.QueryProcessorOptionReducers;
import com.funnelback.publicui.search.lifecycle.input.processors.FacetedNavigationSetQueryProcessorOptions;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.collection.QueryProcessorOption;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.GScopeItem;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.MetadataFieldFill;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.URLFill;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.google.common.collect.Lists;

public class FacetedNavigationSetQueryProcessorOptionsTest {

    private FacetedNavigationSetQueryProcessorOptions processor;
    private QueryProcessorOptionReducers reducers;
    private SearchTransaction transaction;
    private SearchQuestion question;
    
    @Before
    public void before() {
        question = Mockito.mock(SearchQuestion.class);
        Mockito.when(question.getCollection()).thenReturn(new Collection("dummy", null));
        
        transaction = Mockito.mock(SearchTransaction.class);
        Mockito.when(transaction.getQuestion()).thenReturn(question);
        
        reducers = Mockito.mock(QueryProcessorOptionReducers.class);
        
        FacetedNavigationConfig fnConfig = Mockito.mock(FacetedNavigationConfig.class);
        Function<SearchQuestion, FacetedNavigationConfig> fnProvider = (q) -> fnConfig;
        
        processor = new FacetedNavigationSetQueryProcessorOptions();
        processor.setReducers(reducers);
        processor.setFacetedNavigationConfigProvider(fnProvider);
    }
    
    @Test
    public void testNoFacets() throws Exception {
        processor.setFacetedNavigationConfigProvider((q) -> null);
        
        processor.processInput(transaction);
        
        Mockito.verifyZeroInteractions(reducers);
    }
    
    @Test
    public void testFacets() throws Exception {
        // Configure faceted navigation
        CategoryDefinition cDef1 = new MetadataFieldFill("a");
        CategoryDefinition cDef2 = new URLFill("http://example.org/");
        FacetDefinition fDef1 = FacetDefinition.getFacetWithUpgradedValues("facet1", Lists.newArrayList(cDef1, cDef2));

        CategoryDefinition cDef3 = new GScopeItem("categoryName", 12);
        FacetDefinition fDef2 = FacetDefinition.getFacetWithUpgradedValues("facet2", Lists.newArrayList(cDef3));

        FacetedNavigationConfig fnConfig = new FacetedNavigationConfig(Lists.newArrayList(fDef1, fDef2));
        processor.setFacetedNavigationConfigProvider((q) -> fnConfig);
        
        // Mock reducing of QPOs
        Mockito.when(reducers.reduceAllQueryProcessorOptions(Mockito.any()))
            .thenReturn(Lists.newArrayList(Pair.of("option1", "value1"), Pair.of("option2", "value2")));
        
        // Capture generated QPOs
        List<String> dynamicQpOptions = Mockito.mock(List.class);
        Mockito.when(question.getDynamicQueryProcessorOptions()).thenReturn(dynamicQpOptions);
        
        processor.processInput(transaction);

        Mockito.verify(reducers).reduceAllQueryProcessorOptions(Lists.newArrayList(
            new QueryProcessorOption<>("rmcf", "a"),
            new QueryProcessorOption<>("count_urls", 1+0+2),//one for the base depth + zero for the segments in url + 2 for 
                                                            //extra depth to workaround padre limitation.
            new QueryProcessorOption<>("countgbits", "all")));
        
        Mockito.verify(question, Mockito.times(2)).getDynamicQueryProcessorOptions();
        
        Mockito.verify(dynamicQpOptions).add("-option1=value1");
        Mockito.verify(dynamicQpOptions).add("-option2=value2");
    }
    
}
