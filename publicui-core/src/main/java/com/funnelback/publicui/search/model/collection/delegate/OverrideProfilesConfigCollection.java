package com.funnelback.publicui.search.model.collection.delegate;

import java.util.Map;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Profile;

import lombok.Getter;

/**
 * {@link DelegateCollection} to override profiles
 * 
 * @author lbutters@funnelback.com
 */
public class OverrideProfilesConfigCollection extends DelegateCollection {

    @Getter
    private final Map<String, Profile> profiles;

    public OverrideProfilesConfigCollection(Collection collection,
        Map<String, Profile> profiles) {
        super(collection);
        this.profiles = profiles;
    }
}
