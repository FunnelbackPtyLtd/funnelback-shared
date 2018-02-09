package com.funnelback.publicui.utils.web;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.publicui.search.model.collection.Collection;

/**
 * Helps with picking a profile to use in the modern UI.
 * 
 * In the modern UI the profile used is the one the user sets unless it
 * doesn't exist in which case it is set to "default"
 *
 */
public class ProfilePicker {

    public String existingProfileForCollection(Collection collection, String wantedProfile) {
        if (!collection.getProfiles().containsKey(wantedProfile)) {
            return DefaultValues.DEFAULT_PROFILE;
        }
        return wantedProfile;
    }
}
