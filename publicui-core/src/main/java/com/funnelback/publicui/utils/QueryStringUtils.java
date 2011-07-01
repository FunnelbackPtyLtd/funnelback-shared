package com.funnelback.publicui.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
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
	
	public static Map<String, String> toArrayMap(String qs, boolean startsWithQuestionMark) {
		Map<String, List<String>> converted = toMap(qs, startsWithQuestionMark);
		Map<String, String> out = new HashMap<String, String>();
		for (String key : converted.keySet()) {
			if (converted.get(key).size() > 0) {
				out.put(key, converted.get(key).get(0));
			} else {
				out.put(key, null);
			}
		}
		return out;
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
		        if (key != null && ! "".equals(key)) {
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
		}
		return params;
	}

	@SneakyThrows(UnsupportedEncodingException.class)
	public static String toString(Map<String, List<String>> qs, boolean prependQuestionMark) {
		StringBuffer out = new StringBuffer();
		for (Map.Entry<String, List<String>> entry: qs.entrySet()) {
			if (entry.getValue() != null) {
				for (String value: entry.getValue()) {
					out.append("&")
						.append(URLEncoder.encode(entry.getKey(), "UTF-8"))
						.append("=");
					if(value != null) {
						out.append(URLEncoder.encode(value, "UTF-8"));
					}
				}
			} else {
				out.append("&")
				.append(URLEncoder.encode(entry.getKey(), "UTF-8"))
				.append("=");
			}
		}
		return ((prependQuestionMark) ? "?" : "") + out.toString().substring(1);
	}
}
