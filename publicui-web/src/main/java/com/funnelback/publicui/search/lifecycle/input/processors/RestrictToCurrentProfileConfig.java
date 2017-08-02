package com.funnelback.publicui.search.lifecycle.input.processors;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.input.AbstractInputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Profile;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

import lombok.extern.log4j.Log4j2;

@Component("restrictToCurrentProfileConfig")
@Log4j2
public class RestrictToCurrentProfileConfig extends AbstractInputProcessor {

    @Override
    public void processInput(SearchTransaction searchTransaction) throws InputProcessorException {
        searchTransaction.getQuestion().setCollection(
            searchTransaction.getQuestion().getCollection().cloneBuilder()
            .profiles(getProfiles(searchTransaction.getQuestion().getCurrentProfile(), 
                searchTransaction.getQuestion().getCollection())).build());
    }
    
    public Map<String, Profile> getProfiles(String profile, Collection collection) {
        if(profile == null) {
            return Collections.emptyMap();
        }
        Profile p = collection.getProfiles().get(profile);
        if(p == null) {
            return Collections.emptyMap();
        }
        
        HashMap<String, Profile> profiles = new HashMap<>();
        profiles.put(profile, p);
        
        return profiles;
    }

}
