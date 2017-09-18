package com.funnelback.publicui.accessibilityauditor.lifecycle.input.processors;

import static com.funnelback.common.filter.accessibility.metadata.MetadataWhenUsedInCountIndexedTerms.VALUE_IS_RMCF_COMPATIBLE;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.funnelback.common.filter.accessibility.Metadata;
import com.funnelback.common.filter.accessibility.metadata.AllAccessibilityAuditorMetadata;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.google.common.collect.ImmutableList;

import lombok.extern.log4j.Log4j2;
/**
 * If rmcf has been enabled for some particular metadata classes then we will switch to
 * using countIndexTerms instead. We do this as countINdexTerms is an order of magnitude faster.
 * 
 * The criteria to use countIndexTerms is:
 * * the values of the metadata are indexable terms.
 * * the values of the metadata are not repeated within a single document e.g. authoer: david is ok
 * but author: david|david is not.
 *
 */
@Log4j2
@Component("reMapRMCFOptionInputProcessor")
public class ReMapRMCFOptionInputProcessor extends AbstractAccessibilityAuditorInputProcessor {
    
    public static final List<Metadata> MD_TO_RE_MAP = ImmutableList.copyOf(new AllAccessibilityAuditorMetadata()
        .getAllMetadata()
        .filter(m -> VALUE_IS_RMCF_COMPATIBLE == m.getUsedInCountIndexedTerms())        
        .collect(Collectors.toList()));
    
    
    private static final List<String> METADATA_CLASS_NAMES_TO_REMAP = ImmutableList.copyOf(MD_TO_RE_MAP.stream()
        .map(Metadata::asMetadataClass)
        .collect(Collectors.toList()));
    
    
    
    @Override
    public void processAccessibilityAuditorTransaction(SearchTransaction transaction) throws InputProcessorException {
        
        List<String> options = Optional.ofNullable(transaction).map(SearchTransaction::getQuestion)
        .map(SearchQuestion::getDynamicQueryProcessorOptions)
        .orElse(null);
        
        if(options == null) {
            return;
        }
        
        List<String> metadataToCount = new ArrayList<>();
        
        for(int i = 0; i < options.size(); i++) {
            String option = options.get(i);
            if(option.startsWith("-rmcf=[")) {
                option = option.replace("]", ",]");
                for(String mdToRemove : METADATA_CLASS_NAMES_TO_REMAP) {
                    if(option.contains(mdToRemove + ",")) {
                        option = option.replace(mdToRemove + ",", "");
                        metadataToCount.add(mdToRemove);
                    }
                }
                option = option.replace(",,", ",").replace(",]", "]");
                options.set(i, option);
            }
        }
        
        boolean hasIndexedTermsOption = false;
        
        for(int i = 0; i < options.size(); i++) {
            String option = options.get(i);
            if(option.startsWith("-countIndexedTerms=")) {
                // Does it have a [] part?
                if(option.equals("-countIndexedTerms=")) {
                    option += "[";
                }
                if(option.endsWith("]")) {
                    option = option.substring(0, option.length()-1);
                    if(!option.endsWith("[")) {
                        option += ",";
                    }
                }
                option += StringUtils.join(metadataToCount, ",") + "]";
                options.set(i, option);
                hasIndexedTermsOption = true;
            }
        }
        
        if(!hasIndexedTermsOption) {
            options.add("-countIndexedTerms=[" + StringUtils.join(metadataToCount, ",") + "]");
        }
    }

}