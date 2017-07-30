package com.funnelback.publicui.search.lifecycle.input.processors;

import static com.funnelback.publicui.search.model.transaction.SearchTransaction.ExtraSearches.FACETED_NAVIGATION;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.Keys;
import com.funnelback.common.facetednavigation.models.FacetConstraintJoin;
import com.funnelback.common.facetednavigation.models.FacetValues;
import com.funnelback.publicui.search.lifecycle.input.AbstractInputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.extrasearches.FacetedNavigationQuestionFactory;
import com.funnelback.publicui.search.lifecycle.inputoutput.ExtraSearchesExecutor;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetExtraSearchNames;
import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.utils.FacetedNavigationUtils;
import com.funnelback.publicui.utils.PadreOptionsForSpeed;
import com.funnelback.publicui.utils.QueryStringUtils;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;
/**
 * Processor to trigger an extra search without any facets constraints,
 * to be able to always display the full facets list even after a facet
 * has been selected.
 */
@Component("multiFacetedNavigationInputProcessor")
@Log4j2
public class MultiFacetedNavigation extends AbstractInputProcessor {

    //TODO account for CA or AA wanting to use this.
    
    @Autowired @Setter
    private ExtraSearchesExecutor extraSearchesExecutor;
    
    @Override
    public void processInput(SearchTransaction searchTransaction) throws InputProcessorException {
        
        if (SearchTransactionUtils.hasCollection(searchTransaction)
                && searchTransaction.getQuestion().getQuestionType().equals(SearchQuestion.SearchQuestionType.SEARCH)) {
            if(Config.isTrue(searchTransaction.getQuestion()
                    .getCollection().getConfiguration().value(Keys.ModernUI.FULL_FACETS_LIST))
                || doAnyFacetsNeedFullFacetValues(searchTransaction)) {
                SearchQuestion q = new FacetedNavigationQuestionFactory()
                    .buildQuestion(searchTransaction.getQuestion(), null);
                searchTransaction.addExtraSearch(FACETED_NAVIGATION.toString(), q);
                
                addExtraSearchesForOrBasedFacetCounts(searchTransaction);
            }
        }
    }
    
    public void addExtraSearchesForOrBasedFacetCounts(SearchTransaction searchTransaction) {
        Map<String, FacetDefinition> orTypeFacets = getFacetDefinitions(searchTransaction)
        .stream()
        .filter(f -> f.getConstraintJoin() == FacetConstraintJoin.OR)
        .collect(Collectors.toMap(f -> f.getName(), f -> f));
        
        if(orTypeFacets.isEmpty()) {
            log.debug("No OR type facets will not need to run extra searches.");
            return;
        }
        
        log.debug("Or type facets detected waiting for unscoped search to complete.");
        //First get the Un scopped extra search, we will probably have to wait for it.
        Optional<SearchTransaction> unscopedSearch = this.extraSearchesExecutor
            .getAndMaybeWaitForExtraSearch(searchTransaction, FACETED_NAVIGATION.toString());
        
        if(!unscopedSearch.isPresent()) {
            log.warn("Could not get Search Transaction for unscoped query did it fail?");
            return;
        }
        
        for(Facet facet : unscopedSearch.map(SearchTransaction::getResponse).map(SearchResponse::getFacets).orElse(Collections.emptyList())) {
            if(orTypeFacets.containsKey(facet.getName())) {
                for(CategoryValue value : Optional.ofNullable(facet.getUnselectedValues()).orElse(Collections.emptyList())) {
                    String extraSearchName = new FacetExtraSearchNames().getExtraSearchName(facet, value);
                    SearchQuestion extraQuestion = new FacetedNavigationQuestionFactory().buildBasicExtraFacetSearch(searchTransaction.getQuestion());
                    
                    // Update the query as though we had selected the facet value.
                    extraQuestion.getRawInputParameters().keySet().stream().collect(Collectors.toList())
                        .forEach(extraQuestion.getRawInputParameters()::remove);
             
                    
                    QueryStringUtils.toMap(value.getSelectUrl())
                        .forEach((k, v) -> extraQuestion.getRawInputParameters().put(k, v.toArray(new String[0])));
                    
                    // For this query we are interested only in the total matching so lets
                    // not count rmcf or anything else, we need to speed these extra searches up
                    // as we have many of these.
                    new PadreOptionsForSpeed()
                    .getOptionsThatDoNotAffectResultSetAsPairs()
                    .stream()
                    .forEach(p -> extraQuestion.getRawInputParameters().put(p.getOption(), new String[]{p.getValue()}));
                    
                    extraQuestion.getRawInputParameters().put("num_rank", new String[]{"1"});
                    
                    searchTransaction.addExtraSearch(extraSearchName, extraQuestion);
                    log.debug("Added extra search '{}'", extraSearchName);
                    
                }
            }
        }
        
    }
    
    
    public boolean doAnyFacetsNeedFullFacetValues(SearchTransaction st) {
         return Optional.of(getFacetDefinitions(st))
            .map(fl -> fl.stream().anyMatch(f -> f.getFacetValues() == FacetValues.FROM_UNSCOPED_QUERY))
            .orElse(false);
    }
    
    public List<FacetDefinition> getFacetDefinitions(SearchTransaction st) {
        return Optional.ofNullable(FacetedNavigationUtils.selectConfiguration(st))
            .map(c -> c.getFacetDefinitions())
            .orElse(Collections.emptyList());
    }

}
