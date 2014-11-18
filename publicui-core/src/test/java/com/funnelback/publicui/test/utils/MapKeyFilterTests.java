package com.funnelback.publicui.test.utils;

import java.util.HashMap;

import org.junit.Assert;

import org.junit.Test;

import com.funnelback.publicui.utils.MapKeyFilter;

public class MapKeyFilterTests {

    @Test
    public void test() {
        HashMap<String, String> params = new HashMap<String, String>();
        
        params.put("param1", "");
        params.put("param2", "");
        params.put("meta_X", "y");
        params.put("meta_X_or", "y");
        
        MapKeyFilter f = new MapKeyFilter(params);
        
        String[] actual = f.filter("^(p|m).*");
        Assert.assertEquals(4, actual.length);
        Assert.assertEquals("param1", actual[0]);
        Assert.assertEquals("param2", actual[1]);
        Assert.assertEquals("meta_X", actual[2]);
        Assert.assertEquals("meta_X_or", actual[3]);
        
        Assert.assertEquals(1, f.filter(".*X$").length);
        Assert.assertEquals("meta_X", f.filter(".*X$")[0]);
        
        Assert.assertEquals(0, f.filter("test").length);
    }
    
}
