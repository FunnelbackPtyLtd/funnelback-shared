package com.funnelback.publicui.test.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.publicui.utils.SingleValueMapWrapper;

public class SingleValueMapWrapperTests {

	private HashMap<String, String[]> map;
	private SingleValueMapWrapper wrapper;
	
	@Before
	public void before() {
		map = new HashMap<String, String[]>();
		wrapper = new SingleValueMapWrapper(map);
		
		map.put("null", null);
		map.put("0-sized-array", new String[0]);
		map.put("1-slot", new String[] {"1"});
		map.put("3-slots", new String[] {"a", "b", "c"});
	}
	
	@Test
	public void testClear() {
		wrapper.clear();
		Assert.assertTrue(map.isEmpty());
	}
	
	@Test
	public void testContainsKey() {
		Assert.assertTrue(wrapper.containsKey("null"));
		Assert.assertTrue(wrapper.containsKey("0-sized-array"));
		Assert.assertTrue(wrapper.containsKey("1-slot"));
		Assert.assertTrue(wrapper.containsKey("3-slots"));
		Assert.assertFalse(wrapper.containsKey("dummy"));
		Assert.assertFalse(wrapper.containsKey(null));
	}
	
	@Test
	public void testContainsValue() {
		Assert.assertTrue(wrapper.containsValue(null));
		Assert.assertTrue(wrapper.containsValue("1"));
		Assert.assertTrue(wrapper.containsValue("a"));
		Assert.assertFalse(wrapper.containsValue("b"));
		Assert.assertFalse(wrapper.containsValue("c"));
		Assert.assertFalse(wrapper.containsValue("dummy"));
	}
	
	@Test
	public void testEntrySet() {
		Set<Map.Entry<String, String>> entries = wrapper.entrySet();
		Assert.assertEquals(entries.size(), map.entrySet().size());
		
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
			Assert.assertNotNull(entryFound);
			
			// Compare values
			if (value == null || value.length == 0) {
				Assert.assertNull(entryFound.getValue());
			} else {
				Assert.assertEquals(entryFound.getValue(), value[0]);
			}
		}		
	}
	
	@Test
	public void testGet() {
		Assert.assertEquals(null, wrapper.get("null"));
		Assert.assertEquals(null, wrapper.get("0-sized-array"));
		Assert.assertEquals("1", wrapper.get("1-slot"));
		Assert.assertEquals("a", wrapper.get("3-slots"));
		Assert.assertNull(wrapper.get("dummy"));
	}
	
	@Test
	public void testIsEmpty() {
		Assert.assertFalse(wrapper.isEmpty());
		wrapper.clear();
		Assert.assertTrue(wrapper.isEmpty());
		wrapper.put("ab", "cd");
		Assert.assertFalse(wrapper.isEmpty());
		Assert.assertTrue(new SingleValueMapWrapper(new HashMap<String, String[]>()).isEmpty());
	}
	
	@Test
	public void testKeySet() {
		Assert.assertEquals(map.keySet().size(), wrapper.keySet().size());
		
		Set<String> mapKeySet = map.keySet();
		for (String key: wrapper.keySet()) {
			Assert.assertTrue(mapKeySet.contains(key));
		}
	}
	
	@Test
	public void testPut() {
		wrapper.put("newentry", "newvalue");
		Assert.assertTrue(wrapper.containsKey("newentry"));
		Assert.assertTrue(wrapper.containsValue("newvalue"));
		Assert.assertEquals("newvalue", wrapper.get("newentry"));
		Assert.assertTrue(map.containsKey("newentry"));
		Assert.assertArrayEquals(new String[] {"newvalue"}, map.get("newentry"));
	}
	
	@Test
	public void testPutAll() {
		HashMap<String, String> newEntries = new HashMap<String, String>();
		newEntries.put("new1", "value1");
		newEntries.put("new2", null);
		newEntries.put("1-slot", "9");
		
		int sizeBefore = wrapper.size();
		
		wrapper.putAll(newEntries);
		
		Assert.assertEquals(sizeBefore + 2, wrapper.size());
		testContainsKey();
		
		Assert.assertTrue(newEntries.containsKey("new1"));
		Assert.assertTrue(newEntries.containsKey("new2"));
		Assert.assertTrue(newEntries.containsValue("value1"));
		Assert.assertEquals("value1", wrapper.get("new1"));
		Assert.assertEquals(null, wrapper.get("new2"));
		Assert.assertEquals("9", wrapper.get("1-slot"));
		Assert.assertArrayEquals(new String[] {"value1"}, map.get("new1"));
		Assert.assertNull(map.get("new2"));
		Assert.assertArrayEquals(new String[] {"9"}, map.get("1-slot"));
	}
	
	@Test
	public void testCopy() {
		Map<String, String> copy = new HashMap<String, String>(wrapper);
		Assert.assertEquals(copy.size(), wrapper.size());
		
		for (String key: copy.keySet()) {
			Assert.assertEquals(copy.get(key), wrapper.get(key));
		}
	}
	
}

