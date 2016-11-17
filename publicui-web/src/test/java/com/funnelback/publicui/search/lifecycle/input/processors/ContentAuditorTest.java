package com.funnelback.publicui.search.lifecycle.input.processors;



import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;

public class ContentAuditorTest {

    @Test
    public void checkCollectionIsNotModifiedWithCAFacetConfig() {
        SearchQuestion question = new SearchQuestion();
        //Collection is a shared cached object ensure it is not changed.
        Collection collection = mock(Collection.class);
        FacetedNavigationConfig sharedConfig = mock(FacetedNavigationConfig.class);
        when(collection.getFacetedNavigationConfConfig()).thenReturn(sharedConfig);
        when(collection.getFacetedNavigationLiveConfig()).thenReturn(sharedConfig);
        
        
        
        ContentAuditor contentAuditor = spy(new ContentAuditor());
        
        FacetedNavigationConfig caFacetConfig = mock(FacetedNavigationConfig.class);
        doReturn(caFacetConfig).when(contentAuditor).buildFacetConfig(question);
        
        //Call CA to updated the facet
        contentAuditor.updateQuestionWithContentAuditorFacetConfig(question);
        
        //We should find that the collection has not been edited.
        verify(collection, times(0)).setFacetedNavigationConfConfig(any());
        verify(collection, times(0)).setFacetedNavigationLiveConfig(any());
        
        //However the facet config should be returned by the collection now.
        Assert.assertEquals(caFacetConfig, question.getCollection().getFacetedNavigationConfConfig());
        Assert.assertEquals(caFacetConfig, question.getCollection().getFacetedNavigationLiveConfig());
        
        
    }
}
