package com.funnelback.publicui.test.search.lifecycle.input.processors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.funnelback.common.facetednavigation.models.FacetConstraintJoin;
import com.funnelback.publicui.search.lifecycle.input.AbstractInputProcessor;
import com.funnelback.publicui.search.lifecycle.input.processors.FacetedNavigation;
import com.funnelback.publicui.search.lifecycle.input.processors.LegacyFacetedNavigation;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Collection.CollectionBuilder;
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

import lombok.Getter;
import lombok.Setter;

public class BothFacetedNavigationInputProcessors extends AbstractInputProcessor {

    @Getter @Setter private FacetedNavigation facetedNavigation = new FacetedNavigation();
    @Getter @Setter private LegacyFacetedNavigation legacyFacetedNavigation = new LegacyFacetedNavigation();
    
    @Override
    public void processInput(SearchTransaction searchTransaction) {
        this.legacyFacetedNavigation.processInput(searchTransaction);
        this.facetedNavigation.processInput(searchTransaction);
    }
    
    
    public void switchAllFacetConfigToSelectionAnd(SearchTransaction st) {
        CollectionBuilder cloneBuilder = st.getQuestion().getCollection().cloneBuilder();
        
        cloneBuilder.facetedNavigationLiveConfig(switchAllFacetConfigToSelectionAnd(Optional.ofNullable(st)
            .map(SearchTransaction::getQuestion)
            .map(SearchQuestion::getCollection)
            .map(Collection::getFacetedNavigationLiveConfig)
            .orElse(null)));
        
        cloneBuilder.facetedNavigationConfConfig(switchAllFacetConfigToSelectionAnd(Optional.ofNullable(st)
            .map(SearchTransaction::getQuestion)
            .map(SearchQuestion::getCollection)
            .map(Collection::getFacetedNavigationConfConfig)
            .orElse(null)));
        
        st.getQuestion().setCollection(cloneBuilder.build());
    }
    
    
    private FacetedNavigationConfig switchAllFacetConfigToSelectionAnd(FacetedNavigationConfig facetedNavigationConfig) {
        if(facetedNavigationConfig == null) {
            return null;
        }
         return new FacetedNavigationConfig(facetedNavigationConfig.getFacetDefinitions().stream().map(f -> 
            new FacetDefinition(f.getName(), 
                f.getCategoryDefinitions(), 
                f.getSelectionType(), 
                FacetConstraintJoin.AND, 
                f.getFacetValues()))
            .collect(Collectors.toList()));
    }
}
