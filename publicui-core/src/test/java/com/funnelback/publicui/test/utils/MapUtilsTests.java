package com.funnelback.publicui.test.utils;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.publicui.utils.MapUtils;

public class MapUtilsTests {

    private Map<String, String> testData;
    
    @Before
    public void before() {
        testData = new HashMap<String, String>();
        testData.put("key1", "value1a,value1b");
        testData.put("key2", "value2");
        testData.put("key3", null);
    }
    
    @Test
    public void testGetString() {
        Assert.assertEquals("default", MapUtils.getString(testData, "inexistent", "default"));
        Assert.assertEquals("default", MapUtils.getString(testData, "key3", "default"));
        Assert.assertEquals("value1a,value1b", MapUtils.getString(testData, "key1", "default"));
        Assert.assertEquals("value2", MapUtils.getString(testData, "key2", "default"));
    }
    
    @Test
    public void testPutIfNotNull() {
        MapUtils.putIfNotNull(testData, "null", null);
        Assert.assertFalse(testData.containsKey("null"));
        Assert.assertEquals(3, testData.size());

        MapUtils.putIfNotNull(testData, "nonnull", "nonnull");
        Assert.assertTrue(testData.containsKey("nonnull"));
        Assert.assertEquals(4, testData.size());
        Assert.assertEquals("nonnull", testData.get("nonnull"));
    }
    
}
