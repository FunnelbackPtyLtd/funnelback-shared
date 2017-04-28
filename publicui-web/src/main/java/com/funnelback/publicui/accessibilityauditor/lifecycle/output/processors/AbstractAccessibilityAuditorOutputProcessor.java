package com.funnelback.publicui.accessibilityauditor.lifecycle.output.processors;

import com.funnelback.publicui.search.lifecycle.output.AbstractOutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.SearchQuestionType;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;

/**
 * Base class for accessibility auditor output processors. Will process the transaction
 * only of the question is of type {@link SearchQuestionType#ACCESSIBILITY_AUDITOR}.
 * 
 * @author nguillaumin@funnelback.com
 *
 */
public abstract class AbstractAccessibilityAuditorOutputProcessor extends AbstractOutputProcessor {

    @Override
    public void processOutput(SearchTransaction st) throws OutputProcessorException {
        if (SearchTransactionUtils.hasQuestion(st)
            && SearchQuestionType.ACCESSIBILITY_AUDITOR.equals(st.getQuestion().getQuestionType())) {
            processAccessibilityAuditorTransaction(st);
        }
    }
    
    protected abstract void processAccessibilityAuditorTransaction(SearchTransaction transaction) throws OutputProcessorException;
        
}
