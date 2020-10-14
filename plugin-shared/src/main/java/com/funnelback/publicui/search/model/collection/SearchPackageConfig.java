package com.funnelback.publicui.search.model.collection;

import java.util.List;

public interface SearchPackageConfig {
    
    /**
     * <p>Returns the list of component collections for the current collection</p>
     * 
     * <p>Only meta collections (search packages) have component collections</p>
     * 
     * @return The list of component collections for the collection being searched.
     */
    public List<String> getMetaComponents();
}
