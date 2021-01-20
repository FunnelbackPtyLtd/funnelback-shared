package com.funnelback.publicui.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.common.collect.ListMultimap;

/**
 * Filters the keys of a Map depending of a regex.
 */
public class MapKeyFilter {
    
    private String[] keys;
    
    public MapKeyFilter(ListMultimap<String, ?> parameters) {
        keys = parameters.keySet().toArray(new String[0]);
    }
    
    public MapKeyFilter(Map<String, ?> parameters) {
        keys = parameters.keySet().toArray(new String[0]);
    }
    
    public String[] filter(Pattern p) {
        List<String> out = new ArrayList<String>();
        for(String name: keys) {
            if (p.matcher(name).matches()) {
                out.add(name);
            }
        }
        return out.toArray(new String[0]);
    }
    
    public String[] filter(String regex){
        return filter(Pattern.compile(regex));
    }
    
}
