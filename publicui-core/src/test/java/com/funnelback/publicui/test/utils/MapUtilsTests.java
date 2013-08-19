package com.funnelback.publicui.test.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.publicui.utils.MapUtils;

public class MapUtilsTests {

    private Map<String, String> testData;
    private Map<String, String[]> testDataList;
    
    @Before
    public void before() {
        testData = new HashMap<String, String>();
        testData.put("key1", "value1a,value1b");
        testData.put("key2", "value2");
        testData.put("key3", null);
        
        testDataList = new HashMap<String, String[]>();
        testDataList.put("key1", new String[] {"value1a", "value1b"});
        testDataList.put("key2", new String[] {"value2"});
        testDataList.put("key3", null);
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
    
    @Test
    public void testConvertMapList() {
        Map<String, List<String>> out = MapUtils.convertMapList(testDataList);
        
        Assert.assertEquals(3, out.size());
        Assert.assertArrayEquals(new String[] {"value1a", "value1b"}, out.get("key1").toArray(new String[0]));
        Assert.assertArrayEquals(new String[] {"value2"}, out.get("key2").toArray(new String[0]));
        Assert.assertNull(out.get("key3"));
        
    }
    
}
