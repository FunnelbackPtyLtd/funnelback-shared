package com.funnelback.publicui.search.model.collection.delegate;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;

import lombok.Getter;
import lombok.Setter;

/**
 * {@link DelegateCollection} to override the collection level faceted navigation config
 * 
 * @author lbutters@funnelback.com
 *
 */
public class OverrideFacetConfigCollection extends DelegateCollection {

    @Getter
    @Setter
    private FacetedNavigationConfig facetedNavigationConfConfig;

    @Getter
    @Setter
    private FacetedNavigationConfig facetedNavigationLiveConfig;

    public OverrideFacetConfigCollection(Collection collection,
        FacetedNavigationConfig facetedNavigationConfConfig,
        FacetedNavigationConfig facetedNavigationLiveConfig) {
        super(collection);
        this.facetedNavigationConfConfig = facetedNavigationConfConfig;
        this.facetedNavigationLiveConfig = facetedNavigationLiveConfig;
    }
}
