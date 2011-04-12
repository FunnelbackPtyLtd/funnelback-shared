package com.funnelback.publicui.utils;

import java.util.Map;

public class MapUtils {

	public static String getString(Map<String, String[]> map, Object key, String defaultValue) {
		if (map.get(key) != null && map.get(key).length > 0 ) {
			return map.get(key)[0];
		} else {
			return defaultValue;
		}
	}
	
	public static void putIfNotNull(Map<String, String[]> out, String key, String data) {
		if (data != null) {
			out.put(key, new String[] {data});
		}
	}
	
}
