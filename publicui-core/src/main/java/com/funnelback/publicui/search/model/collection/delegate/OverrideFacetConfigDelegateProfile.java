package com.funnelback.publicui.search.model.collection.delegate;

import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.collection.Profile;

import lombok.Getter;
import lombok.Setter;

/**
 * {@link DelegateProfile} to override the faceted navigation configuration
 * 
 * @author lbutters@funnelback.com
 */
public class OverrideFacetConfigDelegateProfile extends DelegateProfile {

    @Getter
    @Setter
    private FacetedNavigationConfig facetedNavConfConfig;

    public OverrideFacetConfigDelegateProfile(Profile profile,
        FacetedNavigationConfig facetedNavConfConfig) {
        super(profile);
        this.facetedNavConfConfig = facetedNavConfConfig;
    }

}
