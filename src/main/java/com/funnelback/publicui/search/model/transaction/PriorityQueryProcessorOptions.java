package com.funnelback.publicui.search.model.transaction;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class PriorityQueryProcessorOptions {
    
    private final Map<String, String> queryProcessorOptions = new HashMap<>();
    
    /**
     * Adds an option to the map of query processor options that are guaranteed to apply.
     * @param key
     * @param value
     */
    public void addOption(@NonNull String key, @NonNull String value) {
        if(queryProcessorOptions.containsKey(key)) {
            if(!value.equals(queryProcessorOptions.get(key))) {
                log.debug("Attempt overwrite priority QPO '{}' was stopped it was not set to {}", key, value);
            }
        } else {
            queryProcessorOptions.put(key, value);
        }
    }
    
    public Map<String, String> getOptions() {
        return ImmutableMap.copyOf(queryProcessorOptions);
    }
    
}
