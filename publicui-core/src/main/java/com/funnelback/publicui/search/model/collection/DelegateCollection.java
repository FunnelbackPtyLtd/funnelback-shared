package com.funnelback.publicui.search.model.collection;

import lombok.experimental.Delegate;

import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * A class that delegates all calls to the given collection.
 * 
 * <p>Internal use only</p>
 * 
 * <p>As a Collection is cached and shared between all request it is best you do NOT edit it.
 * This class can instead be extended and have getter/setter methods overwritten so that the 
 * Collection will return different objects without messing with the shared Collection. 
 * First create a class that overrides the getter/setter methods for the thing you want to
 * override:</p>
 * <code>
 * private class OverridingCollection extends DelegateCollection {
 *       
 *      @Getter @Setter private FacetedNavigationConfig facetedNavigationConfConfig;
 *      
 *      public OverridesFacetConfigCollection(Collection collection,
 *          FacetedNavigationConfig facetedNavigationConfConfig) {
 *          super(collection);
 *          this.facetedNavigationConfConfig = facetedNavigationConfConfig;
 *      }
 *   }
 * </code>
 * <p>Then you should update your {@link SearchTransaction} with your new Collection e.g.:</p>
 * <code>
 * searchTransaction.getQuestion().setCollection(new OverridingCollection(collection, newFacetConfig));
 * </code>
 *  
 *
 */
public class DelegateCollection extends Collection {
    
    //We must use a delegate annotation otherwise our class will go out of sync
    @Delegate
    public Collection collection;
    
    public DelegateCollection(Collection collection) {
        this.collection = collection;
    }
}
