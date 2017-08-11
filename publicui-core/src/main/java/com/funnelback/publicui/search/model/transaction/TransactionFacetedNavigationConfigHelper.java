package com.funnelback.publicui.search.model.transaction;

import java.util.Map;
import java.util.stream.Collectors;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.collection.Profile;

public class TransactionFacetedNavigationConfigHelper {

    /**
     * Updates the search transaction so that the given faceted nav config is used.
     * 
     * This is usefull as it does it such that:
     * * you don't edit the cached version of config.
     * * you update faceted nav in all places.
     * 
     * @param st
     * @param facetedNavigationConfig
     */
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
