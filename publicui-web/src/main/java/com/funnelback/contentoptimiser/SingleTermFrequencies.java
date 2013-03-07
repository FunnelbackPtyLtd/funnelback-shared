package com.funnelback.contentoptimiser;

import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SingleTermFrequencies {

    private final int count;
    @Getter private final int percentageLess;
    
    private final Map<String,Integer> metaDataCounts;
    
    public Integer getCount(String metaDataField) {
        if(metaDataCounts.containsKey(metaDataField)) {
            return metaDataCounts.get(metaDataField);
        } else return 0;
    }
    
    public int getCount() {
        return count;
    }
}
