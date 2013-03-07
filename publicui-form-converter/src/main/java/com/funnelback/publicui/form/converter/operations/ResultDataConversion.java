package com.funnelback.publicui.form.converter.operations;

import lombok.extern.slf4j.Slf4j;

import com.funnelback.publicui.form.converter.Operation;

/**
 * Converts <s:ResultData> tags
 *
 */
@Slf4j
public class ResultDataConversion implements Operation {

    private static final String MODEL_PREFIX = "response.resultPacket";
    
    private static final String[][] MAPPING = {
        {"query", ".query"},
        {"query_as_processed", ".query_as_processed"},
        
        {"fully_matching", ".resultsSummary.fullyMatching"},
        {"partially_matching", ".resultsSummary.partiallyMatching"},
        {"total_matching", ".resultsSummary.totalMatching"},
        {"num_ranks", ".resultsSummary.numRanks"},
        {"currstart", ".resultsSummary.currStart"},
        {"currend", ".resultsSummary.currEnd"},
        {"prevstart", ".resultsSummary.prevStart"},
        {"nextstart", ".resultsSummary.nextStart"},
        {"estimated_hits", ".resultsSummary.estimatedHits"},

        {"padre_version", ".detail.padreVersion"}
    };
    
    @Override
    public String process(String in) {
        String out = in;
        
        log.info("Processing <s:ResultData> tags");
        for (String[] mapping: MAPPING) {
            String previous = mapping[0];
            String actual = mapping[1];
            
            out = out.replaceAll("<s:ResultData>" + previous + "</s:ResultData>", "\\${" + MODEL_PREFIX + actual + "}");
        }
        
        return out;
        
    }

}
