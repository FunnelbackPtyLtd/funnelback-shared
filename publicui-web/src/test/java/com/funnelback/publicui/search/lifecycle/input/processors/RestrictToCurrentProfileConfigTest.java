package com.funnelback.publicui.search.lifecycle.input.processors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Profile;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;



public class RestrictToCurrentProfileConfigTest {

    @Test
    public void testRestrictsToSetProfile() throws Exception {
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);
        st.getQuestion().setCollection(new Collection());
        Profile p1 = mock(Profile.class);
        Profile p2 = mock(Profile.class);
        Map<String, Profile> profilesOrig = st.getQuestion().getCollection().getProfiles();
        st.getQuestion().getCollection().getProfiles().put("p1", p1);
        st.getQuestion().getCollection().getProfiles().put("p2", p2);
        st.getQuestion().setFrontendId("p1");
        
        new RestrictToCurrentFrontendConfig().processInput(st);
        
        Assert.assertEquals("The underlying Map of profiles must not be edited as it is share between requests",
            2, profilesOrig.size());
        
        Assert.assertEquals(1, st.getQuestion().getCollection().getProfiles().size());
        
        Assert.assertEquals(p1, st.getQuestion().getCollection().getProfiles().get("p1"));
    }
    
    @Test
    public void testProfileIsNull() {
        Collection collection = mock(Collection.class);
        
        
        Assert.assertEquals(0, new RestrictToCurrentFrontendConfig().getProfiles(null, collection).size());
    }
    
    @Test
    public void testUnknownProfile() {
        Collection collection = mock(Collection.class);
        when(collection.getProfiles()).thenReturn(new HashMap<>());
        Assert.assertEquals(0, new RestrictToCurrentFrontendConfig().getProfiles("unknown", collection).size());
    }
}
