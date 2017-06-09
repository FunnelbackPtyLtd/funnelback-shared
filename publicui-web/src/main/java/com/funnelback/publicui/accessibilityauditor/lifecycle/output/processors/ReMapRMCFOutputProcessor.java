package com.funnelback.publicui.accessibilityauditor.lifecycle.output.processors;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.funnelback.common.filter.accessibility.Metadata;
import com.funnelback.common.filter.accessibility.metadata.MetadataCase;
import com.funnelback.publicui.accessibilityauditor.lifecycle.input.processors.ReMapRMCFOptionInputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * Once we have used countIndexTerms over rmcf here we place the data we worked out
 * from countIndexTerms into rmcf. In this way the faceted naviagtion machinery doesn't 
 * need to know.
 *
 */
@Component("reMapRMCFOutputProcessor")
public class ReMapRMCFOutputProcessor extends AbstractAccessibilityAuditorOutputProcessor {
    
    private static final Map<String, Metadata> MD_TO_RE_MAP = ReMapRMCFOptionInputProcessor.MD_TO_RE_MAP
        .stream()
        .collect(Collectors.toMap(Metadata::asMetadataClass, m -> m));

    @Override
    protected void processAccessibilityAuditorTransaction(SearchTransaction transaction)
        throws OutputProcessorException {
        ResultPacket resultPacket = Optional.ofNullable(transaction)
        .map(SearchTransaction::getResponse)
        .map(SearchResponse::getResultPacket)
        .orElse(null);
        if(resultPacket != null && resultPacket.getRmcs() != null && resultPacket.getIndexedTermCounts() != null) {
            for(String option : MD_TO_RE_MAP.keySet()) {
                Map<String, Integer> termAndOccurrences = resultPacket.getIndexedTermCounts()
                    .stream()
                    .filter(i -> option.equals(i.getMetadataClass()))
                    .map(i -> i.getTermAndOccurrences())
                    .findFirst()
                    .orElse(new HashMap<>())
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(e -> {
                        if(MD_TO_RE_MAP.get(option).getMetadataCase() == MetadataCase.ALL_LOWER) {
                            return option + ":" + e.getKey().toLowerCase();
                        } else {
                            return option + ":" + e.getKey().toUpperCase();
                        }
                    }, e -> (int)(long) e.getValue()));
                
                resultPacket.getRmcs().keySet().forEach(termAndOccurrences::remove);
                
                resultPacket.getRmcs().putAll(termAndOccurrences);
            }
        }
    }

}
