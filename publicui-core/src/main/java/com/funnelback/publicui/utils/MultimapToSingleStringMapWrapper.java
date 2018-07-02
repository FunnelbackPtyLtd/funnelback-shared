package com.funnelback.publicui.utils;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MultimapToSingleStringMapWrapper implements Map<String, String> {

    private Map<String, List<String>> underlyingMetadata;
    private Map<String, List<String>> separators;
    private Map<String, List<String>> definedSeparators;

    public MultimapToSingleStringMapWrapper(Map<String, List<String>> underlyingMetadata, Map<String, List<String>> separators, Map<String, List<String>> definedSeparators) {
        this.underlyingMetadata = underlyingMetadata;
        this.separators = separators;
        this.definedSeparators = definedSeparators;
    }
    
    @Override
    public int size() {
        return underlyingMetadata.size();
    }

    @Override
    public boolean isEmpty() {
        return underlyingMetadata.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return underlyingMetadata.containsKey(key);
    }

    /**
     * <p>Warning - This check is not efficient on this map wrapper
     * because we must calculate all the values.</p>
     * 
     * {@inheritDoc}
     */
    @Override
    public boolean containsValue(Object value) {
        for(String key : keySet()) {
            if (value.equals(get(key))) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String get(Object key) {
        if (! (key instanceof String)) {
            return null;
        }
        String keyString = (String) key;
        
        List<String> values = underlyingMetadata.get(keyString);
        
        if (values == null) {
            return null;
        }
        
        List<String> valueSeparators = separators.get(keyString);
        
        String fallbackSeparator = "|";
        if (definedSeparators.get(keyString) != null && definedSeparators.get(keyString).size() > 0) {
            fallbackSeparator = definedSeparators.get(keyString).get(0);
        }
        
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            result.append(values.get(i));
            
            if (valueSeparators != null && valueSeparators.size() > i) {
                result.append(valueSeparators.get(i));
            } else {
                if (values.size() > i + 1) {
                    result.append(fallbackSeparator);
                }
            }
        }
        
        return result.toString();
    }

    @Override
    public String put(String key, String value) {
        String previousValue = get(key);
        Set<Character> separatorCharacters = new HashSet<>();
        if (definedSeparators.containsKey(key)) {
            // For now we assume the separators are always one character long - Will need to change
            // this if padre starts supporting longer separators.
            separatorCharacters = definedSeparators.get(key).stream().map((s) -> s.charAt(0)).collect(Collectors.toSet());
        }

        underlyingMetadata.put(key, new ArrayList<String>());
        separators.put(key, new ArrayList<String>());
        
        int start = 0;
        for (int i = 0; i < value.length(); i++) {
            if (separatorCharacters.contains(value.charAt(i))) {
                underlyingMetadata.get(key).add(value.substring(start, i));
                separators.get(key).add(Character.toString(value.charAt(i)));
                start = i + 1;
            }
        }
        // Catch the last value
        underlyingMetadata.get(key).add(value.substring(start));

        return previousValue;
    }

    @Override
    public String remove(Object key) {
        String result = get(key);
        underlyingMetadata.remove(key);
        separators.remove(key);
        return result;
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        for (Map.Entry<? extends String, ? extends String> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        underlyingMetadata.clear();
        separators.clear();
    }

    @Override
    public Set<String> keySet() {
        return underlyingMetadata.keySet();
    }

    @Override
    public Collection<String> values() {
        List<String> result = new ArrayList<>();
        
        for(String key : keySet()) {
            result.add(get(key));
        }
        
        return result;
    }

    @Override
    public Set<java.util.Map.Entry<String, String>> entrySet() {
        Set<Map.Entry<String, String>> result = new HashSet<Map.Entry<String, String>>();
        
        for(String key : keySet()) {
            result.add(new AbstractMap.SimpleImmutableEntry<String, String>(key, get(key)));
        }

        return result;
    }

}
