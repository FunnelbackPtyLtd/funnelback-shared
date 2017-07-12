package com.funnelback.publicui.search.lifecycle.input.processors;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.Keys;
import com.funnelback.common.facetednavigation.models.FacetValues;
import com.funnelback.publicui.search.lifecycle.input.AbstractInputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.extrasearches.FacetedNavigationQuestionFactory;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.utils.FacetedNavigationUtils;

/**
 * Processor to trigger an extra search without any facets constraints,
 * to be able to always display the full facets list even after a facet
 * has been selected.
 */
@Component("multiFacetedNavigationInputProcessor")
public class MultiFacetedNavigation extends AbstractInputProcessor {

    //TODO account for CA or AA wanting to use this.
    
    @Override
    public void processInput(SearchTransaction searchTransaction) throws InputProcessorException {
        
        if (SearchTransactionUtils.hasCollection(searchTransaction)
                && searchTransaction.getQuestion().getQuestionType().equals(SearchQuestion.SearchQuestionType.SEARCH)) {
            if(Config.isTrue(searchTransaction.getQuestion()
                    .getCollection().getConfiguration().value(Keys.ModernUI.FULL_FACETS_LIST))
                || doAnyFacetsNeedFullFacetValues(searchTransaction)) {
                SearchQuestion q = new FacetedNavigationQuestionFactory()
                    .buildQuestion(searchTransaction.getQuestion(), null);
                searchTransaction.addExtraSearch(SearchTransaction.ExtraSearches.FACETED_NAVIGATION.toString(), q);
            }
        }
    }
    
    public boolean doAnyFacetsNeedFullFacetValues(SearchTransaction st) {
         return Optional.ofNullable(FacetedNavigationUtils.selectConfiguration(st))
            .map(c -> c.getFacetDefinitions())
            .map(fl -> fl.stream().anyMatch(f -> f.getFacetValues() == FacetValues.FROM_UNSCOPED_QUERY))
            .orElse(false);
    }

}
