package com.funnelback.publicui.utils;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ListMultimap;

class SharedQueryStringUtilsTests {
    
    @Test
    void testToMap() {
        String input = "param1=value1"
            + "&param2=first+value"
            + "&param2=second%20value"
            + "&param3=%0A%09"
            + "&param4="
            + "&param5=null"
            + "&param6=something=with=equals"
            + "&param7=something%26with%26ampersands"
            + "&foo";
        
        ListMultimap<String, String> map = SharedQueryStringUtils.toMap("?" + input);
        
        Assertions.assertEquals(List.of("value1"), map.get("param1"));
        Assertions.assertEquals(Arrays.asList("first value", "second value"), map.get("param2"));
        Assertions.assertEquals(List.of("\n\t"), map.get("param3"));
        Assertions.assertEquals(List.of(""), map.get("param4"));
        Assertions.assertEquals(List.of("null"), map.get("param5"));
        Assertions.assertEquals(List.of("something=with=equals"), map.get("param6"));
        Assertions.assertEquals(List.of("something&with&ampersands"), map.get("param7"));
        
        map = SharedQueryStringUtils.toMap(input);

        Assertions.assertEquals(List.of("value1"), map.get("param1"));
        Assertions.assertEquals(Arrays.asList("first value", "second value"), map.get("param2"));
        Assertions.assertEquals(List.of("\n\t"), map.get("param3"));
        Assertions.assertEquals(List.of(""), map.get("param4"));
        Assertions.assertEquals(List.of("null"), map.get("param5"));
        Assertions.assertEquals(List.of("something=with=equals"), map.get("param6"));
        Assertions.assertEquals(List.of("something&with&ampersands"), map.get("param7"));
    }
    
    @Test
    void testToMapEmptyString() {
        Assertions.assertTrue(SharedQueryStringUtils.toMap("").isEmpty());
    }
}