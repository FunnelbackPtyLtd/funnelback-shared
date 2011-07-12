package com.funnelback.publicui.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Filters the keys of a Map depending of a regex.
 */
public class MapKeyFilter {

	private static final String[] CONVERSION_ARRAY = new String[0];
	
	private String[] keys;
	
	public MapKeyFilter(Map<String, ?> parameters) {
		keys = parameters.keySet().toArray(CONVERSION_ARRAY);
	}
	
	public String[] filter(Pattern p) {
		List<String> out = new ArrayList<String>();
		for(String name: keys) {
			if (p.matcher(name).matches()) {
				out.add(name);
			}
		}
		return out.toArray(CONVERSION_ARRAY);
	}
	
	public String[] filter(String regex){
		return filter(Pattern.compile(regex));
	}
	
}
