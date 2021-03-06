package com.funnelback.publicui.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import com.google.common.collect.ListMultimap;

public class MapUtils {

    /**
     * Gets a value for a given key, or a default value if the key is not found.
     * Will return the first entry in the String array.
     * @param map
     * @param key
     * @param defaultValue
     * @return
     */
    public static String getFirstString(Map<String, String[]> map, Object key, String defaultValue) {
        if (map.get(key) != null && map.get(key).length > 0 ) {
            return map.get(key)[0];
        } else {
            return defaultValue;
        }
    }
    
    public static String getFirstString(ListMultimap<String, String> map, String key, String defaultValue) {
        return map.get(key).stream().findFirst().orElse(defaultValue);
    }

    public static String getString(Map<String, String> map, Object key, String defaultValue) {
        if (map.get(key) != null ) {
            return map.get(key);
        } else {
            return defaultValue;
        }
    }
    /**
     * Put a String value in a map, transforming it into a 1-slot String array, and only
     * if it's not null.
     * @param out
     * @param key
     * @param data
     */
    public static void putAsStringArrayIfNotNull(Map<String, String[]> out, String key, String data) {
        if (data != null) {
            out.put(key, new String[] {data});
        }
    }
    
    public static void putIfNotNull(ListMultimap<String, String> out, String key, String data) {
        if (data != null) {
            out.put(key, data);
        }
    }
    
    public static void putIfNotNull(Map<String, String> out, String key, String data) {
        if (data != null) {
            out.put(key, data);
        }
    }
    
    /**
     * Converts a {@link Properties} to a {@link Map}
     * @param p Properties to convert
     * @return A {@link Map}
     */
    public static Map<String, String> fromProperties(Properties p) {
        Map<String, String> out = new HashMap<String, String>();
        for (Entry<Object, Object> entry: p.entrySet()) {
            out.put(entry.getKey().toString(), entry.getValue().toString());
        }
        return out;        
    }
    
    /**
     * Converts a <code>{@link Map}&lt;String, String[]&gt;</code>
     * into a <code>{@link Map}&lt;String, List&lt;&lt;String&gt;&gt;</code>
     * @param map Map to convert
     * @return Converted Map
     */
    public static Map<String, List<String>> convertMapList(Map<String, String[]> map) {
        Map<String, List<String>> out = new HashMap<String, List<String>>();
        for (Entry<String, String[]> entry: map.entrySet()) {
            if (entry.getValue() != null) {
                out.put(entry.getKey(), Arrays.asList(entry.getValue()));
            } else {
                out.put(entry.getKey(), null);
            }
        }
        
        return out;
    }
    
    public static Map<String, String[]> convertMapArray(Map<String, List<String>> map) {
        Map<String, String[]> out = new HashMap<>();
        for (Entry<String, List<String>> entry: map.entrySet()) {
            if (entry.getValue() != null) {
                out.put(entry.getKey(), entry.getValue().toArray(new String[0]));
            } else {
                out.put(entry.getKey(), null);
            }
        }
        return out;
    }
    
}
