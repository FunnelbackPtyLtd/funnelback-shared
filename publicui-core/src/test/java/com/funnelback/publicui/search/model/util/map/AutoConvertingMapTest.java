package com.funnelback.publicui.search.model.util.map;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;


@SuppressWarnings({ "rawtypes", "unchecked" })
public class AutoConvertingMapTest {

    @Test
    public void putAsWrongType() {
        AutoConvertingMap map = new AutoConvertingMap<>(Converters.INTEGER_TO_STRING, "", new HashMap<>());
        map.put(new Integer(1), "hi");
        Assert.assertEquals("hi", map.get("1"));
    }
    
    @Test
    public void getAsWrongType() {
        AutoConvertingMap map = new AutoConvertingMap<>(Converters.INTEGER_TO_STRING, "", new HashMap<>());
        map.put("1", "hi");
        Assert.assertEquals("hi", map.get(new Integer(1)));
    }
    
    @Test
    public void containsKeyAsWrongType() {
        AutoConvertingMap map = new AutoConvertingMap<>(Converters.INTEGER_TO_STRING, "", new HashMap<>());
        map.put("1", "hi");
        Assert.assertTrue(map.containsKey(new Integer(1)));
    }
    
    @Test
    public void removeAsWrongType() {
        AutoConvertingMap map = new AutoConvertingMap<>(Converters.INTEGER_TO_STRING, "", new HashMap<>());
        map.put("1", "hi");
        Assert.assertTrue(map.containsKey("1"));
        map.remove(new Integer(1));
        Assert.assertFalse(map.containsKey("1"));
    }
    
    @Test
    public void putAllAsWrongType() {
        AutoConvertingMap map = new AutoConvertingMap<>(Converters.INTEGER_TO_STRING, "", new HashMap<>());
        map.putAll(ImmutableMap.of(new Integer(1), "hi"));
        Assert.assertTrue(map.containsKey("1"));
    }
    
}
