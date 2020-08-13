package com.funnelback.publicui.utils;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Map;

import java.net.URLDecoder;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;

/**
 * Utilities for manipulating query strings.
 *  
 */
public class SharedQueryStringUtils {
    
    /**
     * Convert a query string to a ListMultimap
     * 
     * @param qs Query String
     * @return A {@link Map} representing the query string.
     */
    public static ListMultimap<String, String> toMap(final String qs) {
        ListMultimap<String, String> params = MultimapBuilder.hashKeys().arrayListValues().build();
        
        String query = qs;
        if (query.startsWith("?")) {
            query = query.substring(1);
        }
        
        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);
            String key = URLDecoder.decode(pair[0], UTF_8);
            if (key != null && ! "".equals(key)) {
                if (pair.length > 1) {
                    String value = URLDecoder.decode(pair[1], UTF_8);
                    params.put(key, value);
                }
            }
        }
        
        return params;
    }


}
