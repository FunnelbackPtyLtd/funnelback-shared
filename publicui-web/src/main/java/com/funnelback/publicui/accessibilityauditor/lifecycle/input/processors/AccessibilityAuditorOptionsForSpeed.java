package com.funnelback.publicui.accessibilityauditor.lifecycle.input.processors;

import lombok.extern.log4j.Log4j2;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.utils.PadreOptionsForSpeed;

@Log4j2
@Component("accessibilityAuditorOptionsForSpeed")
public class AccessibilityAuditorOptionsForSpeed extends AbstractAccessibilityAuditorInputProcessor {
    
    protected void processAccessibilityAuditorTransaction(SearchTransaction transaction) throws InputProcessorException {
        PadreOptionsForSpeed optionsForSpeed = new PadreOptionsForSpeed();
        
        //Speed up query processing
        transaction.getQuestion().getDynamicQueryProcessorOptions()
            .addAll(optionsForSpeed.getOptionsThatDoNotAffectResultSet());
        
        //It probably makes sense that we don't reduce the result set when we are auditing the documents.
        transaction.getQuestion().getDynamicQueryProcessorOptions()
            .addAll(optionsForSpeed.getOptionsToTurnOfReducingResultSet());
            
    }

}
