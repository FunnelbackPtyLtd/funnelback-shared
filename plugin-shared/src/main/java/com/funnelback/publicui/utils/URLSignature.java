package com.funnelback.publicui.utils;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import java.net.URLDecoder;

import com.google.common.collect.ListMultimap;

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
    public static String canonicaliseQueryStringToBeHashed(String queryString) {
        List<String> keyList = new ArrayList<String>();
        List<String> valueList = new ArrayList<String>();
        
        ListMultimap<String, String> params = SharedQueryStringUtils.toMap(queryString);
        SortedSet<String> sortedKeys = new TreeSet<String>(params.keySet());
        for (String key: sortedKeys) {
            keyList.add(URLDecoder.decode(key, UTF_8));
            if (params.containsKey(key)) {
                for (String value: params.get(key)) {
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
