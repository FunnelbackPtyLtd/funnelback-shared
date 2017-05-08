package com.funnelback.publicui.accessibilityauditor.lifecycle.input.processors;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.utils.PadreOptionsForSpeed;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component("accessibilityAuditorOptionsForSpeed")
public class AccessibilityAuditorOptionsForSpeed extends AbstractAccessibilityAuditorInputProcessor {
    
    protected void processAccessibilityAuditorTransaction(SearchTransaction transaction) throws InputProcessorException {
        PadreOptionsForSpeed optionsForSpeed = new PadreOptionsForSpeed();
        
        // Always want high service volume
        transaction.getQuestion().getDynamicQueryProcessorOptions()
            .add(optionsForSpeed.getHighServiceVolumeOption());
        
        //Speed up query processing
        transaction.getQuestion().getDynamicQueryProcessorOptions()
            .addAll(optionsForSpeed.getOptionsThatDoNotAffectResultSet());
        
        //It probably makes sense that we don't reduce the result set when we are auditing the documents.
        transaction.getQuestion().getDynamicQueryProcessorOptions()
            .addAll(optionsForSpeed.getOptionsToTurnOfReducingResultSet());
        
        
    }

}
