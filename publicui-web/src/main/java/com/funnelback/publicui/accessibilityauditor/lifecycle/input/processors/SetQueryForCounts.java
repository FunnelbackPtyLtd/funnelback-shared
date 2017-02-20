package com.funnelback.publicui.accessibilityauditor.lifecycle.input.processors;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * Set a default query if none is specified, so that the reports will always
 * have something to show
 * 
 *
 */
@Component("accessibilityAuditorAcknowledgmentCountsSetQuery")
public class SetQueryForCounts extends AbstractAccessibilityAuditorAcknowledgmentCountsInputProcessor {

    /** Query to run if no query is specified - should return all results */
    private static final String NULL_QUERY = "-FunUnusedMetaClass:showalldocuments";
    
    @Override
    protected void processAccessibilityAuditorTransaction(SearchTransaction transaction) throws InputProcessorException {
        if (transaction.getQuestion().getQuery() == null || transaction.getQuestion().getQuery().isEmpty()) {
            transaction.getQuestion().setQuery(NULL_QUERY);
        }
    }

}
