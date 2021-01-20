package com.funnelback.publicui.search.model.collection;

import java.util.Set;

public interface ServiceConfig {

    public String get(String key);
    
    public Set<String> getRawKeys();
    
}
