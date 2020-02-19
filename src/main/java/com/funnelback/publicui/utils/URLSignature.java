package com.funnelback.publicui.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import lombok.SneakyThrows;

/**
 * Utilities to compute a signature for a given URL
 *
 */
public class URLSignature {

    /**
     * <p>Computes a signature for a query string which is independent from the
     * way parameters are encoded, or from their order.</p>
     * 
     * <p>For example the signature for the two following query string will
     * be identical:
     * <ul>
     *  <li><code>param+1=value%201&param%202=value+2</code></li>
     *  <li><code>param+2=value%202&param%201=value+1</code></li>
     * </ul>
     * </p>
     * 
     * <p>Leading question mark on the query string will be ignored.</p>
     * @param queryString Query string to compute parameters for
     * @return The signature for the query string
     */
    
    public static int computeQueryStringSignature(String queryString) {
        return canonicaliseQueryStringToBeHashed(queryString).hashCode();
    }
    
    /**
     * <p>Converts a query string into a canonical form that can be used for hashing.</p>
     * 
     * <p>Known bug: this does suffer some issues like p=2&v=22 would be considered the
     * same as p=22&v=2.<p> 
     * @param queryString
     * @return
     */
    @SneakyThrows(UnsupportedEncodingException.class)
    public static String canonicaliseQueryStringToBeHashed(String queryString) {
        List<String> keyList = new ArrayList<String>();
        List<String> valueList = new ArrayList<String>();
        
        Map<String, String[]> params = QueryStringUtils.toArrayMap(queryString);
        SortedSet<String> sortedKeys = new TreeSet<String>(params.keySet());
        for (String key: sortedKeys) {
            keyList.add(URLDecoder.decode(key, "UTF-8"));
            String[] values = params.get(key);
            if (values != null) {
                for (String value: values) {
                    valueList.add(value);
                }
            }
        }
        
        StringBuilder sb = new StringBuilder();
        keyList.forEach(sb::append);
        valueList.forEach(sb::append);
        return sb.toString();
    }
    
}
