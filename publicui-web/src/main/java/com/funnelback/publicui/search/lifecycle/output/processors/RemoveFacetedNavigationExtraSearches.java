package com.funnelback.publicui.search.lifecycle.output.processors;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.config.keys.Keys.FrontEndKeys.ModernUI;
import com.funnelback.publicui.search.lifecycle.output.AbstractOutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetExtraSearchNames;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.SearchQuestionType;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.search.service.ConfigRepository;

import lombok.Setter;
/**
 * 
 *
 */
@Component("removeFacetedNavigationExtraSearches")
public class RemoveFacetedNavigationExtraSearches extends AbstractOutputProcessor {

    private FacetExtraSearchNames facetExtraSearchNames = new FacetExtraSearchNames();
    
    @Autowired
    @Setter
    private ConfigRepository configRepository;
    
    @Override
    public void processOutput(SearchTransaction searchTransaction) throws OutputProcessorException {
        if (SearchTransactionUtils.hasQuestion(searchTransaction) && 
            searchTransaction.getQuestion().getQuestionType() == SearchQuestionType.SEARCH) {
            if(searchTransaction.getQuestion().getCurrentProfileConfig().get(ModernUI.REMOVE_INTERNAL_FACET_EXTRA_SEARCHES)) {
                searchTransaction.getExtraSearches().keySet()
                .stream()
                .filter(facetExtraSearchNames::isFacetExtraSearch)
                .collect(Collectors.toList()) // Avoid traversing and editing the Map at the same time.
                .stream()
                .forEach(searchTransaction.getExtraSearches()::remove);
            }
        }
    }
}
