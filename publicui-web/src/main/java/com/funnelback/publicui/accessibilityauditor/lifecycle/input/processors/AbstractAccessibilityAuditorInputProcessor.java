package com.funnelback.publicui.accessibilityauditor.lifecycle.input.processors;

import com.funnelback.publicui.search.lifecycle.input.AbstractInputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.SearchQuestionType;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;

/**
 * Base class for accessibility auditor input processors. Will process the transaction
 * only of the question is of type {@link SearchQuestionType#ACCESSIBILITY_AUDITOR}.
 * 
 * @author nguillaumin@funnelback.com
 *
 */
public abstract class AbstractAccessibilityAuditorInputProcessor extends AbstractInputProcessor {

    @Override
    public void processInput(SearchTransaction st) throws InputProcessorException {
        if (SearchTransactionUtils.hasQuestion(st)
            && SearchQuestionType.ACCESSIBILITY_AUDITOR.equals(st.getQuestion().getQuestionType())) {
            processAccessibilityAuditorTransaction(st);
        }
    }
    
    protected abstract void processAccessibilityAuditorTransaction(SearchTransaction transaction) throws InputProcessorException;
        
}
