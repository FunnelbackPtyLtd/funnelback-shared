package com.funnelback.publicui.test.utils;

import java.util.Arrays;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.utils.SharedQueryStringUtils;
import com.google.common.collect.ListMultimap;

public class SharedQueryStringUtilsTests {
    
    @Test
    public void testToMap() {
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
        
        Assert.assertEquals(Arrays.asList("value1"), map.get("param1"));
        Assert.assertEquals(Arrays.asList("first value", "second value"), map.get("param2"));
        Assert.assertEquals(Arrays.asList("\n\t"), map.get("param3"));
        Assert.assertEquals(Arrays.asList(""), map.get("param4"));
        Assert.assertEquals(Arrays.asList("null"), map.get("param5"));
        Assert.assertEquals(Arrays.asList("something=with=equals"), map.get("param6"));
        Assert.assertEquals(Arrays.asList("something&with&ampersands"), map.get("param7"));
        
        map = SharedQueryStringUtils.toMap(input);

        Assert.assertEquals(Arrays.asList("value1"), map.get("param1"));
        Assert.assertEquals(Arrays.asList("first value", "second value"), map.get("param2"));
        Assert.assertEquals(Arrays.asList("\n\t"), map.get("param3"));
        Assert.assertEquals(Arrays.asList(""), map.get("param4"));
        Assert.assertEquals(Arrays.asList("null"), map.get("param5"));
        Assert.assertEquals(Arrays.asList("something=with=equals"), map.get("param6"));
        Assert.assertEquals(Arrays.asList("something&with&ampersands"), map.get("param7"));
    }
    
    @Test
    public void testToMapEmptyString() {
        Assert.assertEquals(new HashMap<>(), SharedQueryStringUtils.toMap(""));
    }
    
}
