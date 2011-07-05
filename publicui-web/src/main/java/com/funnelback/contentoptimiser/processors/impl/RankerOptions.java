package com.funnelback.contentoptimiser.processors.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RankerOptions {
	
	private final Map<String,Double> metaWeights = new HashMap<String,Double>();

	public RankerOptions(String optionsString) {
		Pattern getMetaWeights = Pattern.compile("-wmeta (.) ([^ ]+)");
		Matcher m = getMetaWeights.matcher(optionsString);
		while(m.find()) {
			metaWeights.put(m.group(1), Double.parseDouble(m.group(2)));
		}
	}

	public double getMetaWeight(String metaClass) {
		if(metaWeights.containsKey(metaClass)) return metaWeights.get(metaClass);
		return 1;
	}

}
