package com.funnelback.publicui.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import lombok.SneakyThrows;

/**
 * Utilities for manipulating query strings
 */
public class QueryStringUtils {
    
    /**
     * Convert a query string to Map
     * 
     * @param qs Query string
     * @return A {@link Map} representing the query string.
     */
    public static Map<String, String[]> toArrayMap(final String qs) {
        Map<String, List<String>> converted = toMap(qs);
        Map<String, String[]> out = new HashMap<String, String[]>();
        for (String key : converted.keySet()) {
            if (converted.get(key).size() > 0) {
                out.put(key, converted.get(key).toArray(new String[0]));
            } else {
                out.put(key, null);
            }
        }
        return out;
    }
    
    /**
     * Convert a query string to a Map
     * 
     * @param qs Query String
     * @return A {@link Map} representing the query string.
     */
    @SneakyThrows(UnsupportedEncodingException.class)
    public static Map<String, List<String>> toMap(final String qs) {
        Map<String, List<String>> params = new HashMap<String, List<String>>();
        
        String query = qs;
        if (query.startsWith("?")) {
            query = query.substring(1);
        }
        
        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);
            String key = URLDecoder.decode(pair[0], "UTF-8");
            if (key != null && ! "".equals(key)) {
                String value = null;
                if (pair.length > 1) {
                    value = URLDecoder.decode(pair[1], "UTF-8");
                }
                List<String> values = params.get(key);
                if (values == null) {
                    values = new ArrayList<String>();
                    params.put(key, values);
                }
                if (value != null) {
                    values.add(value);
                }
            }
        }
        
        return params;
    }


}
