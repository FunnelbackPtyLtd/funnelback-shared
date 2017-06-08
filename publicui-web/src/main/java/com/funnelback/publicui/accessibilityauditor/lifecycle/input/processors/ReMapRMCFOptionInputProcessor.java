package com.funnelback.publicui.accessibilityauditor.lifecycle.input.processors;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

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

    /** Query to run if no query is specified - should return all results */
    private static final String NULL_QUERY = "-FunUnusedMetaClass:showalldocuments";
    
    public static final List<String> MD_TO_RE_MAP = ImmutableList.of(
        "FunAASetOfFailingTechniques","FunAAExplicitFailedLevels","FunAAFormat",
        "FunAASetOfFailingPrinciples");
    
    @Override
    protected void processAccessibilityAuditorTransaction(SearchTransaction transaction) throws InputProcessorException {
        if(new File("/tmp/dont").exists()) {
            return;
        }
        List<String> options = Optional.ofNullable(transaction).map(SearchTransaction::getQuestion)
        .map(SearchQuestion::getDynamicQueryProcessorOptions)
        .orElse(new ArrayList<>());
        
        List<String> metadataToCount = new ArrayList<>();
        
        for(int i = 0; i < options.size(); i++) {
            String option = options.get(i);
            if(option.startsWith("-rmcf=")) {
                option = option.replace("]", ",]");
                for(String mdToRemove : MD_TO_RE_MAP) {
                    if(option.contains(mdToRemove + ",")) {
                        option = option.replace(mdToRemove + ",", "");
                        metadataToCount.add(mdToRemove);
                    }
                }
                option.replace(",,", ",");
                options.set(i, option);
            }
        }
        
        for(int i = 0; i < options.size(); i++) {
            String option = options.get(i);
            if(option.startsWith("-countIndexedTerms=")) {
                option = option.substring(0, option.length()-1);
                option = "," + StringUtils.join(metadataToCount, ",") + "]";
                options.set(i, option);
            }
        }
        
        
    }

}