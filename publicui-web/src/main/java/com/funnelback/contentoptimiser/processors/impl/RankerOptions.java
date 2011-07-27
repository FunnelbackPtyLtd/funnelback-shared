package com.funnelback.contentoptimiser.processors.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RankerOptions {
	
	private final Map<String,Double> metaWeights = new HashMap<String,Double>();

	public RankerOptions() {
		metaWeights.put("_", new Double(1));
	}
	
	public void consume(String optionsString) {
		
		String [] A = optionsString.split("\\s+");
		for(String option : A) {
			if("-sco7".equals(option) || "-sco2".equals(option)) {
				metaWeights.put("k", new Double(0.5));
				metaWeights.put("K", new Double(0.5));
				metaWeights.put("t", new Double(1));
			} else if(option.startsWith("-sco7") || option.startsWith("-sco2")) {
				for(int i =5; i < option.length() ; i++ ) {
					Double put = new Double(1);
					if(option.charAt(i) == 'k' || option.charAt(i) == 'K') {
						put = new Double(0.5);
					}
					metaWeights.put("" +option.charAt(i),put);
				}
			}
		}
		
		Pattern getMetaWeights = Pattern.compile("-wmeta (.) ([^ ]+)");
		Matcher m = getMetaWeights.matcher(optionsString);
		while(m.find()) {
			metaWeights.put(m.group(1), Double.parseDouble(m.group(2)));
		}
		
	}

	public double getMetaWeight(String metaClass) {
		if(metaWeights.containsKey(metaClass)) return metaWeights.get(metaClass);
		return 0;
	}

}
