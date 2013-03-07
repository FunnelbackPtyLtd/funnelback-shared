package com.funnelback.publicui.utils;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SingleValueMapWrapper implements Map<String, String>{

    private Map<String, String[]> map;
    
    public SingleValueMapWrapper(Map<String, String[]> map) {
        this.map = map;
    }
    
    @Override
    public int size() {
        return map.size();
    }
    
    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }
    
    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }
    
    @Override
    public boolean containsValue(Object value) {
        return values().contains(value);
    }
    
    @Override
    public String get(Object key) {
        if (map.get(key) != null && map.get(key).length > 0) {
            return map.get(key)[0];
        } else {
            return null;
        }
    }
    
    @Override
    public String put(String key, String value) {
        String oldValue = get(key);
        if (value != null) {
            map.put(key, new String[] {value});
        } else {
            // Don't put a new String[] {null}
            map.put(key, null);
        }
        return oldValue;
    }
    
    @Override
    public String remove(Object key) {
        String value = get(key);
        map.remove(key);
        return value;
    }
    
    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        for (Map.Entry<? extends String, ? extends String> entry: m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
        
    }
    
    @Override
    public void clear() {
        map.clear();
    }
    
    @Override
    public Set<String> keySet() {
        return map.keySet();
    }
    
    @Override
    public Collection<String> values() {
        Set<String> values = new HashSet<String>();
        for (Map.Entry<String, String[]> entry: map.entrySet()) {
            if (entry.getValue() == null || entry.getValue().length == 0) {
                values.add(null);
            } else {
                values.add(entry.getValue()[0]);
            }
        }
        return values;
    }
    @Override
    public Set<java.util.Map.Entry<String, String>> entrySet() {
        Set<Map.Entry<String, String>> entries = new HashSet<Map.Entry<String,String>>();
        for (Map.Entry<String, String[]> entry: map.entrySet()) {
            if (entry.getValue() == null || entry.getValue().length == 0) {
                entries.add(new AbstractMap.SimpleImmutableEntry<String, String>(entry.getKey(), null));
            } else {
                entries.add(new AbstractMap.SimpleImmutableEntry<String, String>(entry.getKey(), entry.getValue()[0]));
            }
        }
        return entries;
    }
    

}
