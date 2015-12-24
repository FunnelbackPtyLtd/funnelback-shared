package com.funnelback.publicui.test.utils;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.utils.QueryStringUtils;

public class QueryStringUtilsTests {

    @Test
    public void testToString() {
        TreeMap<String, List<String>> qs = new TreeMap<String, List<String>>();
        
        qs.put("param1", Arrays.asList(new String[] {"single-value"}));
        qs.put("param2", Arrays.asList(new String[] {"first value", "second value"}));
        qs.put("param3", Arrays.asList(new String[] {"\n\t"}));
        qs.put("param4", null);
        qs.put("param5", Arrays.asList(new String[] {""}));
        
        Assert.assertEquals(
                "param1=single-value"
                + "&param2=first+value&param2=second+value"
                + "&param3=%0A%09"
                + "&param4="
                + "&param5=",
                QueryStringUtils.toString(qs, false));

        Assert.assertEquals(
                "?param1=single-value"
                + "&param2=first+value&param2=second+value"
                + "&param3=%0A%09"
                + "&param4="
                + "&param5=",
                QueryStringUtils.toString(qs, true));
    }
    
    @Test
    public void testToMap() {
        String input = "param1=value1"
            + "&param2=first+value"
            + "&param2=second%20value"
            + "&param3=%0A%09"
            + "&param4="
            + "&param5=null"
            + "&param6=something=with=equals"
            + "&param7=something%26with%26ampersands";
        
        Map<String, List<String>> map = QueryStringUtils.toMap("?" + input);
        
        Assert.assertEquals(Arrays.asList(new String[] {"value1"}), map.get("param1"));
        Assert.assertEquals(Arrays.asList(new String[] {"first value", "second value"}), map.get("param2"));
        Assert.assertEquals(Arrays.asList(new String[] {"\n\t"}), map.get("param3"));
        Assert.assertEquals(Arrays.asList(new String[0]), map.get("param4"));
        Assert.assertEquals(Arrays.asList(new String[] {"null"}), map.get("param5"));
        Assert.assertEquals(Arrays.asList(new String[] {"something=with=equals"}), map.get("param6"));
        Assert.assertEquals(Arrays.asList(new String[] {"something&with&ampersands"}), map.get("param7"));
        
        map = QueryStringUtils.toMap(input);

        Assert.assertEquals(Arrays.asList(new String[] {"value1"}), map.get("param1"));
        Assert.assertEquals(Arrays.asList(new String[] {"first value", "second value"}), map.get("param2"));
        Assert.assertEquals(Arrays.asList(new String[] {"\n\t"}), map.get("param3"));
        Assert.assertEquals(Arrays.asList(new String[0]), map.get("param4"));
        Assert.assertEquals(Arrays.asList(new String[] {"null"}), map.get("param5"));
        Assert.assertEquals(Arrays.asList(new String[] {"something=with=equals"}), map.get("param6"));
        Assert.assertEquals(Arrays.asList(new String[] {"something&with&ampersands"}), map.get("param7"));
    }
    
}
