package com.funnelback.publicui.search.lifecycle.output.processors;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.output.AbstractOutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetExtraSearchNames;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.SearchQuestionType;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
/**
 * 
 *
 */
@Component("removeFacetedNavigationExtraSearches")
public class RemoveFacetedNavigationExtraSearches extends AbstractOutputProcessor {

    private FacetExtraSearchNames facetExtraSearchNames = new FacetExtraSearchNames();
    
    @Override
    public void processOutput(SearchTransaction searchTransaction) throws OutputProcessorException {
        if (SearchTransactionUtils.hasQuestion(searchTransaction) && 
            searchTransaction.getQuestion().getQuestionType() == SearchQuestionType.SEARCH) {
            searchTransaction.getExtraSearches().keySet()
                .stream()
                .filter(facetExtraSearchNames::isFacetExtraSearch)
                .collect(Collectors.toList()) // Avoid traversing and editing the Map at the same time.
                .stream()
                .forEach(searchTransaction.getExtraSearches()::remove);
        }
    }
}
