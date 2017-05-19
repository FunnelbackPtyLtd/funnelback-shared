package com.funnelback.publicui.streamedresults;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import lombok.Getter;

public class ResultFields {

    @Getter List<String> xPaths;
    @Getter List<String> fieldNames;
    
    /**
     * Creates a ResultField from a optional list of xpaths and field names.
     * 
     * We attempt to be kind in that:
     * - If both lists are empty or not present we will set a default value so something is returned.
     * - If the xPaths list is longer we will add values to the fieldNames list.
     * - If the fieldNames list is longer we will add dummy values to the xPaths so that null
     * is returned in those fields.
     * 
     * @param rawXPaths
     * @param rawFieldNames
     */
    public ResultFields(Optional<List<String>> rawXPaths, Optional<List<String>> rawFieldNames) {
        xPaths = new ArrayList<>(rawXPaths.orElse(Collections.emptyList()));
        fieldNames = new ArrayList<>(rawFieldNames.orElse(Collections.emptyList()));
        
        // Add dummy xpaths if we don't have enough.
        for(int i = xPaths.size(); i < fieldNames.size(); i++) {
            xPaths.add("DummyPathToGetANull");
        }
        
        // Add fieldnames that are the same as the xpath if we don't have enough field names.
        for(int i = fieldNames.size(); i < xPaths.size(); i++) {
            fieldNames.add(xPaths.get(i));
        }
        
        
        // By default return the list of URLs in the index.
        if(xPaths.size() == 0) {
            xPaths.add("liveUrl");
            fieldNames.add("Live URL");
        }
    }
    
}
