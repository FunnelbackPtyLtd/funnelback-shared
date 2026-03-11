package com.funnelback.publicui.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MapUtilsTests {

    private Map<String, String> testData;
    private Map<String, String[]> testDataList;
    
    @BeforeEach
    void before() {
        testData = new HashMap<>();
        testData.put("key1", "value1a,value1b");
        testData.put("key2", "value2");
        testData.put("key3", null);
        
        testDataList = new HashMap<>();
        testDataList.put("key1", new String[] {"value1a", "value1b"});
        testDataList.put("key2", new String[] {"value2"});
        testDataList.put("key3", null);
    }
    
    @Test
    void testGetString() {
        Assertions.assertEquals("default", MapUtils.getString(testData, "inexistent", "default"));
        Assertions.assertEquals("default", MapUtils.getString(testData, "key3", "default"));
        Assertions.assertEquals("value1a,value1b", MapUtils.getString(testData, "key1", "default"));
        Assertions.assertEquals("value2", MapUtils.getString(testData, "key2", "default"));
    }
    
    @Test
    void testPutIfNotNull() {
        MapUtils.putIfNotNull(testData, "null", null);
        Assertions.assertFalse(testData.containsKey("null"));
        Assertions.assertEquals(3, testData.size());

        MapUtils.putIfNotNull(testData, "nonnull", "nonnull");
        Assertions.assertTrue(testData.containsKey("nonnull"));
        Assertions.assertEquals(4, testData.size());
        Assertions.assertEquals("nonnull", testData.get("nonnull"));
    }
    
    @Test
    void testConvertMapList() {
        Map<String, List<String>> out = MapUtils.convertMapList(testDataList);
        
        Assertions.assertEquals(3, out.size());
        Assertions.assertArrayEquals(new String[] {"value1a", "value1b"}, out.get("key1").toArray(new String[0]));
        Assertions.assertArrayEquals(new String[] {"value2"}, out.get("key2").toArray(new String[0]));
        Assertions.assertNull(out.get("key3"));
    }
    
    @Test
    void testGetStringWithEmptyMap() {
        Map<String, String> emptyMap = new HashMap<>();
        Assertions.assertEquals("default", MapUtils.getString(emptyMap, "anykey", "default"));
        Assertions.assertEquals("", MapUtils.getString(emptyMap, "anykey", ""));
    }
    
    @Test
    void testPutIfNotNullWithEmptyMap() {
        Map<String, String> emptyMap = new HashMap<>();
        MapUtils.putIfNotNull(emptyMap, "key", "value");
        Assertions.assertEquals(1, emptyMap.size());
        Assertions.assertEquals("value", emptyMap.get("key"));
        
        MapUtils.putIfNotNull(emptyMap, "nullkey", null);
        Assertions.assertEquals(1, emptyMap.size());
        Assertions.assertFalse(emptyMap.containsKey("nullkey"));
    }
    
    @Test
    void testConvertMapListWithEmptyMap() {
        Map<String, String[]> emptyMap = new HashMap<>();
        Map<String, List<String>> out = MapUtils.convertMapList(emptyMap);
        Assertions.assertNotNull(out);
        Assertions.assertTrue(out.isEmpty());
    }
    
    @Test
    void testConvertMapListWithEmptyArray() {
        Map<String, String[]> testData = new HashMap<>();
        testData.put("empty", new String[0]);
        Map<String, List<String>> out = MapUtils.convertMapList(testData);
        Assertions.assertNotNull(out.get("empty"));
        Assertions.assertTrue(out.get("empty").isEmpty());
    }
}