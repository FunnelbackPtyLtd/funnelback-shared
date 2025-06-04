package com.funnelback.publicui.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SingleValueMapWrapperTests {

    private HashMap<String, String[]> map;
    private SingleValueMapWrapper wrapper;
    
    @BeforeEach
    void before() {
        map = new HashMap<>();
        wrapper = new SingleValueMapWrapper(map);
        
        map.put("null", null);
        map.put("0-sized-array", new String[0]);
        map.put("1-slot", new String[] {"1"});
        map.put("3-slots", new String[] {"a", "b", "c"});
    }
    
    @Test
    void testClear() {
        wrapper.clear();
        Assertions.assertTrue(map.isEmpty());
    }
    
    @Test
    void testContainsKey() {
        Assertions.assertTrue(wrapper.containsKey("null"));
        Assertions.assertTrue(wrapper.containsKey("0-sized-array"));
        Assertions.assertTrue(wrapper.containsKey("1-slot"));
        Assertions.assertTrue(wrapper.containsKey("3-slots"));
        Assertions.assertFalse(wrapper.containsKey("dummy"));
        Assertions.assertFalse(wrapper.containsKey(null));
    }
    
    @Test
    void testContainsValue() {
        Assertions.assertTrue(wrapper.containsValue(null));
        Assertions.assertTrue(wrapper.containsValue("1"));
        Assertions.assertTrue(wrapper.containsValue("a"));
        Assertions.assertFalse(wrapper.containsValue("b"));
        Assertions.assertFalse(wrapper.containsValue("c"));
        Assertions.assertFalse(wrapper.containsValue("dummy"));
    }
    
    @Test
    void testEntrySet() {
        Set<Map.Entry<String, String>> entries = wrapper.entrySet();
        Assertions.assertEquals(entries.size(), map.size());
        
        for (Map.Entry<String, String[]> mapEntry: map.entrySet()) {
            String key = mapEntry.getKey();
            String[] value = mapEntry.getValue();
            
            // Find entry with this key
            Map.Entry<String, String> entryFound = null;
            for (Map.Entry<String, String> entry: entries) {
                if (entry.getKey().equals(key)) {
                    entryFound = entry;
                    break;
                }
            }
            Assertions.assertNotNull(entryFound);
            
            // Compare values
            if (value == null || value.length == 0) {
                Assertions.assertNull(entryFound.getValue());
            } else {
                Assertions.assertEquals(entryFound.getValue(), value[0]);
            }
        }        
    }
    
    @Test
    void testGet() {
        Assertions.assertNull(wrapper.get("null"));
        Assertions.assertNull(wrapper.get("0-sized-array"));
        Assertions.assertEquals("1", wrapper.get("1-slot"));
        Assertions.assertEquals("a", wrapper.get("3-slots"));
        Assertions.assertNull(wrapper.get("dummy"));
    }
    
    @Test
    void testIsEmpty() {
        Assertions.assertFalse(wrapper.isEmpty());
        wrapper.clear();
        Assertions.assertTrue(wrapper.isEmpty());
        wrapper.put("ab", "cd");
        Assertions.assertFalse(wrapper.isEmpty());
        Assertions.assertTrue(new SingleValueMapWrapper(new HashMap<>()).isEmpty());
    }
    
    @Test
    void testKeySet() {
        Assertions.assertEquals(map.size(), wrapper.size());
        
        Set<String> mapKeySet = map.keySet();
        for (String key: wrapper.keySet()) {
            Assertions.assertTrue(mapKeySet.contains(key));
        }
    }
    
    @Test
    void testPut() {
        wrapper.put("newentry", "newvalue");
        Assertions.assertTrue(wrapper.containsKey("newentry"));
        Assertions.assertTrue(wrapper.containsValue("newvalue"));
        Assertions.assertEquals("newvalue", wrapper.get("newentry"));
        Assertions.assertTrue(map.containsKey("newentry"));
        Assertions.assertArrayEquals(new String[] {"newvalue"}, map.get("newentry"));
    }
    
    @Test
    void testPutAll() {
        HashMap<String, String> newEntries = new HashMap<>();
        newEntries.put("new1", "value1");
        newEntries.put("new2", null);
        newEntries.put("1-slot", "9");
        
        int sizeBefore = wrapper.size();
        
        wrapper.putAll(newEntries);
        
        Assertions.assertEquals(sizeBefore + 2, wrapper.size());
        testContainsKey();
        
        Assertions.assertTrue(newEntries.containsKey("new1"));
        Assertions.assertTrue(newEntries.containsKey("new2"));
        Assertions.assertTrue(newEntries.containsValue("value1"));
        Assertions.assertEquals("value1", wrapper.get("new1"));
        Assertions.assertNull(wrapper.get("new2"));
        Assertions.assertEquals("9", wrapper.get("1-slot"));
        Assertions.assertArrayEquals(new String[] {"value1"}, map.get("new1"));
        Assertions.assertNull(map.get("new2"));
        Assertions.assertArrayEquals(new String[] {"9"}, map.get("1-slot"));
    }
    
    @Test
    void testCopy() {
        Map<String, String> copy = new HashMap<>(wrapper);
        Assertions.assertEquals(copy.size(), wrapper.size());
        
        for (String key: copy.keySet()) {
            Assertions.assertEquals(copy.get(key), wrapper.get(key));
        }
    }
}