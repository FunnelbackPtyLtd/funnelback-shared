package com.funnelback.publicui.search.model.transaction;

import java.util.Map;
import java.util.stream.Collectors;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.collection.Profile;

public class TransactionFacetedNavigationConfigHelper {

    
    public void updateTheFacetConfigToUse(SearchTransaction st, 
                                            FacetedNavigationConfig facetedNavigationConfig) {
        
        Map<String, Profile> profiles = st.getQuestion().getCollection().getProfiles().entrySet()
            .stream()
            .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().cloneBuilder()
                .facetedNavConfConfig(facetedNavigationConfig)
                .build()));
            
            Collection c = st.getQuestion().getCollection().cloneBuilder()
                            .facetedNavigationConfConfig(facetedNavigationConfig)
                            .facetedNavigationLiveConfig(facetedNavigationConfig)
                            .profiles(profiles)
                            .build();
                

            st.getQuestion().setCollection(c);
    }
}
