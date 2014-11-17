package com.funnelback.publicui.search.lifecycle.input.processors;

import java.util.SortedSet;
import java.util.TreeSet;

import lombok.extern.log4j.Log4j;

import org.apache.commons.lang3.StringUtils;
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
@Log4j
public class CliveMapping extends AbstractInputProcessor {

    @Override
    public void processInput(SearchTransaction searchTransaction) throws InputProcessorException {
        if (SearchTransactionUtils.hasQuestion(searchTransaction)
                && SearchTransactionUtils.hasCollection(searchTransaction)
                && searchTransaction.getQuestion().getClive() != null
                && searchTransaction.getQuestion().getClive().length > 0) {
            
            SortedSet<String> clives = new TreeSet<String>();
            for (String clive: searchTransaction.getQuestion().getClive()) {
                if (clive.matches("\\d+")) {
                    // Already a number, pass it through
                    // That can happen with links built by PADRE (Contextual Nav. links)
                    clives.add(clive);
                } else {
                    for (int i=0; i<searchTransaction.getQuestion().getCollection().getMetaComponents().length; i++) {
                        if (clive.equals(searchTransaction.getQuestion().getCollection().getMetaComponents()[i])) {
                            clives.add(Integer.toString(i));
                            break;
                        }
                    }
                }
            }
    
            if (clives.size() > 0) {
                if (log.isDebugEnabled()) {
                    log.debug("Transformed clive parameter from '"
                            + StringUtils.join(searchTransaction.getQuestion().getClive(), ",")
                            + "' to '" + StringUtils.join(clives, ",") + "'");
                }
    
                searchTransaction.getQuestion().getAdditionalParameters().put(RequestParameters.CLIVE, clives.toArray(new String[0]));
            }
        }

    }

}
