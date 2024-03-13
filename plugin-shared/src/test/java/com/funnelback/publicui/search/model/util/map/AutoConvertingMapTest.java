package com.funnelback.publicui.search.model.util.map;

import java.util.HashMap;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class AutoConvertingMapTest {

    @Test
    public void putAsWrongType() {
        AutoConvertingMap map = new AutoConvertingMap<>(Converters.INTEGER_TO_STRING, "", new HashMap<>());
        map.put(1, "hi");
        Assertions.assertEquals("hi", map.get("1"));
    }
    
    @Test
    public void getAsWrongType() {
        AutoConvertingMap map = new AutoConvertingMap<>(Converters.INTEGER_TO_STRING, "", new HashMap<>());
        map.put("1", "hi");
        Assertions.assertEquals("hi", map.get(1));
    }
    
    @Test
    public void containsKeyAsWrongType() {
        AutoConvertingMap map = new AutoConvertingMap<>(Converters.INTEGER_TO_STRING, "", new HashMap<>());
        map.put("1", "hi");
        Assertions.assertTrue(map.containsKey(1));
    }
    
    @Test
    public void removeAsWrongType() {
        AutoConvertingMap map = new AutoConvertingMap<>(Converters.INTEGER_TO_STRING, "", new HashMap<>());
        map.put("1", "hi");
        Assertions.assertTrue(map.containsKey("1"));
        map.remove(1);
        Assertions.assertFalse(map.containsKey("1"));
    }
    
    @Test
    public void putAllAsWrongType() {
        AutoConvertingMap map = new AutoConvertingMap<>(Converters.INTEGER_TO_STRING, "", new HashMap<>());
        map.putAll(ImmutableMap.of(1, "hi"));
        Assertions.assertTrue(map.containsKey("1"));
    }
    
}