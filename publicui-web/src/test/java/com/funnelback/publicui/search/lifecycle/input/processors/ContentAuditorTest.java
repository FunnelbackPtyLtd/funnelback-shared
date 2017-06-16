package com.funnelback.publicui.search.lifecycle.input.processors;



import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.collection.Profile;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.utils.FacetedNavigationUtils;

public class ContentAuditorTest {

    @Test
    public void checkCollectionIsNotModifiedWithCAFacetConfig() {
        SearchQuestion question = new SearchQuestion();
        FacetedNavigationConfig sharedConfig = mock(FacetedNavigationConfig.class);
        
        //Collection is a shared cached object ensure it is not changed.
        Collection collection = spy(new Collection().cloneBuilder()
            .facetedNavigationConfConfig(sharedConfig)
            .facetedNavigationLiveConfig(sharedConfig)
            .build());
        
        question.setCollection(collection);
        
        
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
        
        
        //Integration check verify that we will actually pick the config.
        Assert.assertEquals("After updating the Collection to have the new facet config"
            + " the selectConfiguration method should be returning the facet config.",
            caFacetConfig, FacetedNavigationUtils.selectConfiguration(question.getCollection(), null));
    }
    
    @Test
    public void testCAOverridesProfileConfig() {
        SearchQuestion question = new SearchQuestion();
        question.setProfile("_default");
        //Collection is a shared cached object ensure it is not changed.
        Collection collection = new Collection();
        FacetedNavigationConfig sharedConfig = mock(FacetedNavigationConfig.class);
        collection.setFacetedNavigationConfConfig(sharedConfig);
        collection.setFacetedNavigationLiveConfig(sharedConfig);
        
        
        //Make this collection have a profile
        Map<String, Profile> profiles = new HashMap<>();
        collection = collection.cloneBuilder().profiles(profiles).build();
        Profile profile = new Profile();
        // And the profile has a faceted nav config.
        profile.setFacetedNavConfConfig(sharedConfig);
        profiles.put("_default", profile);
        
        question.setCollection(collection);
        
        
        
        ContentAuditor contentAuditor = spy(new ContentAuditor());
        
        FacetedNavigationConfig caFacetConfig = mock(FacetedNavigationConfig.class);
        doReturn(caFacetConfig).when(contentAuditor).buildFacetConfig(question);
        
        //Call CA to updated the facet
        contentAuditor.updateQuestionWithContentAuditorFacetConfig(question);
        
        //Integration check verify that we will actually pick the CA facet config
        //rather than a config the profiles actual facet config.
        Assert.assertEquals("After updating the Collection to have the new facet config"
            + " the selectConfiguration method should be returning the facet config.",
            caFacetConfig, FacetedNavigationUtils.selectConfiguration(question.getCollection(), "_default"));
        
        Assert.assertSame(caFacetConfig, 
            question.getCollection().getProfiles().get("_default").getFacetedNavConfConfig());
        
        Assert.assertSame(caFacetConfig, question.getCollection().getFacetedNavigationConfConfig());
        Assert.assertSame(caFacetConfig, question.getCollection().getFacetedNavigationLiveConfig());
    }
}
