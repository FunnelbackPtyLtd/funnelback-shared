package com.funnelback.publicui.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.apache.commons.lang3.StringUtils;

import lombok.SneakyThrows;

/**
 * Utilities for manipulating query strings
 */
public class QueryStringUtils {

    /**
     * <p>Convert a query string to a Map</p>
     * 
     * <p>For each parameter only one value will be retained</p>
     * 
     * @param qs Query string
     * @return A {@link Map} representing the query string.
     */
    public static Map<String, String> toSingleMap(final String qs) {
        Map<String, List<String>> converted = toMap(qs);
        Map<String, String> out = new HashMap<String, String>();
        for (String key : converted.keySet()) {
            if (converted.get(key).size() > 0) {
                out.put(key, converted.get(key).get(0));
            } else {
                out.put(key, null);
            }
        }
        return out;
    }
    
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
            String[] pair = param.split("=");
            String key = URLDecoder.decode(pair[0], "UTF-8");
            if (key != null && ! "".equals(key)) {
                String value = null;
                if (pair.length > 1) {
                    // If there are more than 1 part, join them back with "="
                    // "=" is permitted unencoded within a query string parameter
                    value = URLDecoder.decode(StringUtils.join(pair, "=", 1, pair.length), "UTF-8");
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

    /**
     * Convert a {@link Map} of parameters into a query string
     * @param qs {@link Map} of parameters
     * @param prependQuestionMark Whether the resulting query string should start with a question mark
     * @return The query string
     */
    @SneakyThrows(UnsupportedEncodingException.class)
    public static String toString(Map<String, List<String>> qs, boolean prependQuestionMark) {
        if (qs.isEmpty()) {
            return "";
        }
        
        StringBuffer out = new StringBuffer();
        for (Map.Entry<String, List<String>> entry: qs.entrySet()) {
            if (entry.getValue() != null) {
                for (String value: entry.getValue()) {
                    out.append("&")
                        .append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                        .append("=");
                    if(value != null) {
                        out.append(URLEncoder.encode(value, "UTF-8"));
                    }
                }
            } else {
                out.append("&")
                    .append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                    .append("=");
            }
        }
        return (prependQuestionMark ? "?" : "") + out.toString().substring(1);
    }
    
    public static String arrayMapAsString(Map<String, String[]> map) {
        StringBuilder sb = new StringBuilder();
        for(Entry<String, String[]> e : map.entrySet()) {
            sb.append(e.getKey()).append("\n");
            for(String v : e.getValue()) {
                sb.append("\t") .append(v).append("\n");
            }
        }
        return sb.toString();
    }
    
    public static String listMapAsString(Map<String, List<String>> map) {
        StringBuilder sb = new StringBuilder();
        for(Entry<String, List<String>> e : map.entrySet()) {
            sb.append(e.getKey()).append("\n");
            for(String v : e.getValue()) {
                sb.append("\t") .append(v).append("\n");
            }
        }
        return sb.toString();
    }
}
