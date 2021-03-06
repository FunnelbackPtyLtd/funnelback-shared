package com.funnelback.publicui.test.utils;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.utils.MapKeyFilter;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;

public class MapKeyFilterTests {

    @Test
    public void test() {
        ListMultimap<String, String> params = MultimapBuilder.hashKeys().arrayListValues().build();
        
        params.put("param1", "");
        params.put("param2", "");
        params.put("meta_X", "y");
        params.put("meta_X_or", "y");
        
        MapKeyFilter f = new MapKeyFilter(params);
        
        String[] actual = f.filter("^(p|m).*");
        Assert.assertEquals(4, actual.length);
        Assert.assertTrue(Arrays.asList(actual).contains("param1"));
        Assert.assertTrue(Arrays.asList(actual).contains("param2"));
        Assert.assertTrue(Arrays.asList(actual).contains("meta_X"));
        Assert.assertTrue(Arrays.asList(actual).contains("meta_X_or"));
        
        Assert.assertEquals(1, f.filter(".*X$").length);
        Assert.assertEquals("meta_X", f.filter(".*X$")[0]);
        
        Assert.assertEquals(0, f.filter("test").length);
    }
    
}
