package com.funnelback.contentoptimiser;

import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SingleTermFrequencies {

	private final int count;
	@Getter private final int percentageLess;
	
	private final Map<String,Integer> metaDataCounts;
	
	public Integer getCount(String metaDataField) {
		return metaDataCounts.get(metaDataField);
	}
	
	public Set<Map.Entry<String,Integer>> getCounts() {
		return metaDataCounts.entrySet();
	}

	public int getCount() {
		return count;
	}
}
