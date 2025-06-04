package com.funnelback.publicui.utils;

import java.util.Arrays;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MapKeyFilterTests {

    @Test
    void test() {
        ListMultimap<String, String> params = MultimapBuilder.hashKeys().arrayListValues().build();
        
        params.put("param1", "");
        params.put("param2", "");
        params.put("meta_X", "y");
        params.put("meta_X_or", "y");
        
        MapKeyFilter f = new MapKeyFilter(params);
        
        String[] actual = f.filter("^(p|m).*");
        Assertions.assertEquals(4, actual.length);
        Assertions.assertTrue(Arrays.asList(actual).contains("param1"));
        Assertions.assertTrue(Arrays.asList(actual).contains("param2"));
        Assertions.assertTrue(Arrays.asList(actual).contains("meta_X"));
        Assertions.assertTrue(Arrays.asList(actual).contains("meta_X_or"));
        
        Assertions.assertEquals(1, f.filter(".*X$").length);
        Assertions.assertEquals("meta_X", f.filter(".*X$")[0]);
        
        Assertions.assertEquals(0, f.filter("test").length);
    }
}