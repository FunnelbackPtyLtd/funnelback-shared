package com.funnelback.publicui.search.lifecycle.input.processors;

import lombok.extern.log4j.Log4j2;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.input.AbstractInputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;

/**
 * Maps human-friendly clive names to PADRE internal index
 * from meta.cfg 
 *
 */
@Component("cliveMappingInputProcessor")
@Log4j2
public class CliveMapping extends AbstractInputProcessor {

    @Override
    public void processInput(SearchTransaction searchTransaction) throws InputProcessorException {
        if (SearchTransactionUtils.hasQuestion(searchTransaction)
                && SearchTransactionUtils.hasCollection(searchTransaction)
                && searchTransaction.getQuestion().getClive() != null
                && searchTransaction.getQuestion().getClive().length > 0) {
            
            String[] clive = searchTransaction.getQuestion().getClive();
            String[] copy = new String[clive.length]; 
            System.arraycopy(clive, 0, copy, 0, clive.length);
            searchTransaction
                .getQuestion()
                .getAdditionalParameters()
                .put(RequestParameters.CLIVE, copy);
        }

    }

}
