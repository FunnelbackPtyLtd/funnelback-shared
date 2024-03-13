package com.funnelback.publicui.test.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.funnelback.publicui.utils.SingleValueMapWrapper;

public class SingleValueMapWrapperTests {

    private HashMap<String, String[]> map;
    private SingleValueMapWrapper wrapper;
    
    @BeforeEach
    public void before() {
        map = new HashMap<>();
        wrapper = new SingleValueMapWrapper(map);
        
        map.put("null", null);
        map.put("0-sized-array", new String[0]);
        map.put("1-slot", new String[] {"1"});
        map.put("3-slots", new String[] {"a", "b", "c"});
    }
    
    @Test
    public void testClear() {
        wrapper.clear();
        Assertions.assertTrue(map.isEmpty());
    }
    
    @Test
    public void testContainsKey() {
        Assertions.assertTrue(wrapper.containsKey("null"));
        Assertions.assertTrue(wrapper.containsKey("0-sized-array"));
        Assertions.assertTrue(wrapper.containsKey("1-slot"));
        Assertions.assertTrue(wrapper.containsKey("3-slots"));
        Assertions.assertFalse(wrapper.containsKey("dummy"));
        Assertions.assertFalse(wrapper.containsKey(null));
    }
    
    @Test
    public void testContainsValue() {
        Assertions.assertTrue(wrapper.containsValue(null));
        Assertions.assertTrue(wrapper.containsValue("1"));
        Assertions.assertTrue(wrapper.containsValue("a"));
        Assertions.assertFalse(wrapper.containsValue("b"));
        Assertions.assertFalse(wrapper.containsValue("c"));
        Assertions.assertFalse(wrapper.containsValue("dummy"));
    }
    
    @Test
    public void testEntrySet() {
        Set<Map.Entry<String, String>> entries = wrapper.entrySet();
        Assertions.assertEquals(entries.size(), map.entrySet().size());
        
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
    public void testGet() {
        Assertions.assertNull(wrapper.get("null"));
        Assertions.assertNull(wrapper.get("0-sized-array"));
        Assertions.assertEquals("1", wrapper.get("1-slot"));
        Assertions.assertEquals("a", wrapper.get("3-slots"));
        Assertions.assertNull(wrapper.get("dummy"));
    }
    
    @Test
    public void testIsEmpty() {
        Assertions.assertFalse(wrapper.isEmpty());
        wrapper.clear();
        Assertions.assertTrue(wrapper.isEmpty());
        wrapper.put("ab", "cd");
        Assertions.assertFalse(wrapper.isEmpty());
        Assertions.assertTrue(new SingleValueMapWrapper(new HashMap<>()).isEmpty());
    }
    
    @Test
    public void testKeySet() {
        Assertions.assertEquals(map.keySet().size(), wrapper.keySet().size());
        
        Set<String> mapKeySet = map.keySet();
        for (String key: wrapper.keySet()) {
            Assertions.assertTrue(mapKeySet.contains(key));
        }
    }
    
    @Test
    public void testPut() {
        wrapper.put("newentry", "newvalue");
        Assertions.assertTrue(wrapper.containsKey("newentry"));
        Assertions.assertTrue(wrapper.containsValue("newvalue"));
        Assertions.assertEquals("newvalue", wrapper.get("newentry"));
        Assertions.assertTrue(map.containsKey("newentry"));
        Assertions.assertArrayEquals(new String[] {"newvalue"}, map.get("newentry"));
    }
    
    @Test
    public void testPutAll() {
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
    public void testCopy() {
        Map<String, String> copy = new HashMap<>(wrapper);
        Assertions.assertEquals(copy.size(), wrapper.size());
        
        for (String key: copy.keySet()) {
            Assertions.assertEquals(copy.get(key), wrapper.get(key));
        }
    }
}