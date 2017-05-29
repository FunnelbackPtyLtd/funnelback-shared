package com.funnelback.publicui.search.lifecycle.input.processors;

import java.util.Collections;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.input.AbstractInputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Profile;
import com.funnelback.publicui.search.model.collection.delegate.DelegateCollection;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.google.common.collect.ImmutableMap;

@Component("restrictToCurrentProfileConfig")
public class RestrictToCurrentProfileConfig extends AbstractInputProcessor {

    @Override
    public void processInput(SearchTransaction searchTransaction) throws InputProcessorException {
        searchTransaction.getQuestion().setCollection(
            new SinlgeProfileOnlyCollection(searchTransaction.getQuestion().getCollection(), 
                searchTransaction.getQuestion().getProfile()));
    }
    
    public static class SinlgeProfileOnlyCollection extends DelegateCollection {

        private final String profile;
        
        public SinlgeProfileOnlyCollection(Collection collection, String profile) {
            super(collection);
            this.profile = profile;
        }
        
        public Map<String, Profile> getProfiles() {
            if(profile == null) {
                return Collections.emptyMap();
            }
            Profile p = super.getProfiles().get(profile);
            if(p == null) {
                return Collections.emptyMap();
            }
            
            return ImmutableMap.of(profile, p);
        }
        
    }

}
