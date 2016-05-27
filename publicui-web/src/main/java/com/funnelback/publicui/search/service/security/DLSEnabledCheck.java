package com.funnelback.publicui.search.service.security;

import com.funnelback.publicui.search.model.collection.Collection;

public interface DLSEnabledCheck {

    /**
     * Checks if DLS is enabled for a collection.
     * 
     * @param collection
     * @return
     * @throws IllegalArgumentException if called on a meta collection.
     */
    public boolean isDLSEnabled(Collection collection) throws IllegalArgumentException;
    
}
