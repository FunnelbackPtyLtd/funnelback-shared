package com.funnelback.publicui.web.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.SneakyThrows;

public class QueryStringUtils {

	public static Map<String, List<String>> toMap(String qs, boolean startsWithQuestionMark) {
		if (startsWithQuestionMark) {
			return toMap(qs);
		} else {
			return toMap("?" + qs);
		}
	}
	
	@SneakyThrows(UnsupportedEncodingException.class)
	public static Map<String, List<String>> toMap(String qs) {
		Map<String, List<String>> params = new HashMap<String, List<String>>();
		String[] urlParts = qs.split("\\?");
		if (urlParts.length > 1) {
		    String query = urlParts[1];
		    for (String param : query.split("&")) {
		        String[] pair = param.split("=");
		        String key = URLDecoder.decode(pair[0], "UTF-8");
		        String value = null;
		        if (pair.length > 1) {
		        	value = URLDecoder.decode(pair[1], "UTF-8");
		        }
		        List<String> values = params.get(key);
	        	if (values == null) {
	        		values = new ArrayList<String>();
	        		params.put(key, values);
	        	}
		        if (value != null) {
		        	values.add(value);
		        }
		    }
		}
		return params;
	}
	
}
