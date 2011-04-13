package com.funnelback.publicui.utils;

import java.util.Map;

public class MapUtils {

	/**
	 * Gets a value for a given key, or a default value if the key is not found.
	 * Will return the first entry in the String array.
	 * @param map
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static String getString(Map<String, String[]> map, Object key, String defaultValue) {
		if (map.get(key) != null && map.get(key).length > 0 ) {
			return map.get(key)[0];
		} else {
			return defaultValue;
		}
	}
	
	/**
	 * Put a String value in a map, transforming it into a 1-slot String array, and only
	 * if it's not null.
	 * @param out
	 * @param key
	 * @param data
	 */
	public static void putIfNotNull(Map<String, String[]> out, String key, String data) {
		if (data != null) {
			out.put(key, new String[] {data});
		}
	}
	
}
