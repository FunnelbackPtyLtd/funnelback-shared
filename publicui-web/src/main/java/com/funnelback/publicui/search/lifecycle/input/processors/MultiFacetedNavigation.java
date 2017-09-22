package com.funnelback.publicui.search.lifecycle.input.processors;

import static com.funnelback.common.function.Predicates.not;
import static com.funnelback.publicui.search.model.collection.facetednavigation.FacetExtraSearchNames.SEARCH_FOR_ALL_VALUES;
import static com.funnelback.publicui.search.model.collection.facetednavigation.FacetExtraSearchNames.SEARCH_FOR_UNSCOPED_VALUES;
import static com.funnelback.publicui.search.model.transaction.SearchTransaction.ExtraSearches.FACETED_NAVIGATION;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.Keys;
import com.funnelback.common.facetednavigation.models.FacetValues;
import static com.funnelback.common.facetednavigation.models.FacetValues.*;
import com.funnelback.common.function.Flattener;
import com.funnelback.common.padre.QueryProcessorOptionKeys;
import com.funnelback.publicui.search.lifecycle.input.AbstractInputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.extrasearches.FacetedNavigationQuestionFactory;
import com.funnelback.publicui.search.lifecycle.inputoutput.ExtraSearchesExecutor;
import com.funnelback.publicui.search.lifecycle.output.processors.facetednavigation.FillFacetUrls;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetExtraSearchNames;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetedNavigationProperties;
import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.utils.FacetedNavigationUtils;
import com.funnelback.publicui.utils.MapUtils;
import com.funnelback.publicui.utils.PadreOptionsForSpeed;
import com.funnelback.publicui.utils.PadreOptionsForSpeed.OptionAndValue;
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
    
    @Autowired @Setter
    private ExtraSearchesExecutor extraSearchesExecutor;
    private PadreOptionsForSpeed padreOptionsForSpeed = new PadreOptionsForSpeed();
    
    private FacetedNavigationProperties facetedNavProps = new FacetedNavigationProperties();
    
    @Setter
    private FacetExtraSearchNames facetExtraSearchNames = new FacetExtraSearchNames();
    
    @Override
    public void processInput(SearchTransaction searchTransaction) throws InputProcessorException {
        
        // Currently this wont work for AA or CA.
        if (SearchTransactionUtils.hasCollection(searchTransaction)
                && searchTransaction.getQuestion().getQuestionType().equals(SearchQuestion.SearchQuestionType.SEARCH)) {
            if(Config.isTrue(searchTransaction.getQuestion()
                    .getCollection().getConfiguration().value(Keys.ModernUI.FULL_FACETS_LIST))){
                SearchQuestion q = new FacetedNavigationQuestionFactory()
                    .buildQuestion(searchTransaction.getQuestion(), null);
                
                searchTransaction.addExtraSearch(FACETED_NAVIGATION.toString(), q);
            }
            
            if(doAnyFacetsNeedUnscopedQuery(searchTransaction)) {
                addExtraSearchToGetUnscopedValues(searchTransaction);
            } else {
                log.trace("Not running unscoped query for facets.");
            }
            
            if(doAnyFacetsNeedAllQuery(searchTransaction)) {
                addExtraSearchToGetAllValues(searchTransaction);
            } else {
                log.trace("Not running ALL query for facets, this might be because no facet"
                    + " uses values from the ALL query all because all facets that do only"
                    + " consist of categories where the user defines all the values.");
            }
            
            addSearchesToWorkOutCountsForRadio(searchTransaction);
            
            addSearchesForFacetsWithValuesFromScopedQueryWithFacetUnselected(searchTransaction);
            
            addDedicatedExtraSearchesForOrFacetCounts(searchTransaction);
        }
        
        
    }
    
    /**
     * Unlike the faceted nav extra search configured by config this one is will be faster.
     * 
     * @param searchTransaction
     * @throws InputProcessorException
     */
    public void addExtraSearchToGetUnscopedValues(SearchTransaction searchTransaction) throws InputProcessorException {
        SearchQuestion extraQuestion = extraSearchFacetsUnselectedAndCountsPossible(searchTransaction);

        searchTransaction.addExtraSearch(SEARCH_FOR_UNSCOPED_VALUES, extraQuestion);
    }
    
    public void addExtraSearchToGetAllValues(SearchTransaction searchTransaction) throws InputProcessorException {
        SearchQuestion extraQuestion = extraSearchFacetsUnselectedAndCountsPossible(searchTransaction);
        
        // We want to get all values the user can see so run a padre null search.
        extraQuestion.setQuery("!FunDoesNotExist:padrenull");
            
        searchTransaction.addExtraSearch(SEARCH_FOR_ALL_VALUES, extraQuestion);
    }
    
    /**
     * Gives you a copy of the original search question but with:
     * - no facets selected
     * - qp options to speed things up without disbling counting options (needed to count values in facets)
     * - the original query is enabled.
     * @param searchTransaction
     * @return
     * @throws InputProcessorException
     */
    private SearchQuestion extraSearchFacetsUnselectedAndCountsPossible(SearchTransaction searchTransaction) 
            throws InputProcessorException {
        SearchQuestion extraQuestion = new FacetedNavigationQuestionFactory()
            .buildQuestion(searchTransaction.getQuestion(), null);
        
        applySpeedUpOptionsWithoutTurningOfFacetCounting(extraQuestion);
        
        return extraQuestion;
    }
    
    private void applySpeedUpOptionsWithoutTurningOfFacetCounting(SearchQuestion extraQuestion) {
        // Apply options to speed things up but take care to not apply ones that turn off counting which is need
        // by faceted navigation.
        Set<String> optionsToNotAdd = padreOptionsForSpeed.getOptionsForCounting();
        
        addFacetExtraSearchSpecificPadreOptions(extraQuestion, 
            padreOptionsForSpeed.getOptionsThatDoNotAffectResultSetAsPairs()
            .stream()
            .filter(o -> !optionsToNotAdd.contains(o.getOption()))
            .collect(Collectors.toList())); // Remove options we need for faceted nav.
    }
    
    /**
     * Gives you a copy of the orignal search question with:
     * - The original selected facets left as is.
     * - qp options to speed things up without disabling counting options (need to count values in facets)
     * - thr origanl query enabled.
     * 
     * @param searchTransaction
     * @return
     * @throws InputProcessorException
     */
    private SearchQuestion extraSearchFacetsSelectedAndCountsPossible(SearchTransaction searchTransaction) {
        SearchQuestion extraQuestion = new FacetedNavigationQuestionFactory()
            .buildBasicExtraFacetSearch(searchTransaction.getQuestion());
        
        applySpeedUpOptionsWithoutTurningOfFacetCounting(extraQuestion);
        
        return extraQuestion;
    }
    
    /**
     * Radio means to unselect the current facet then select another value.
     * 
     * So to calculate the values we can just unselect the facet (if selected) run
     * a search and then use the rmcf/gscopes etc sums as the counts for the radio
     * facet.
     * 
     * 
     * @param st
     * @throws InputProcessorException 
     */
    public void addSearchesToWorkOutCountsForRadio(SearchTransaction st) {
        getFacetDefinitions(st).stream()
        .filter(f -> facetedNavProps.useScopedSearchWithFacetDisabledForCounts(f, st))
        .forEach(f -> addScopedSearchWithFacetUnselected(st, f));
    }
    
    /**
     * Adds extra searches for facets which use FROM_SCOPED_QUERY_WITH_FACET_UNSELECTED
     * @param st
     */
    public void addSearchesForFacetsWithValuesFromScopedQueryWithFacetUnselected(SearchTransaction st) {
        getFacetDefinitions(st).stream()
        .filter(f -> facetedNavProps.useScopedSearchWithFacetDisabledForValues(f, st))
        .forEach(f -> addScopedSearchWithFacetUnselected(st, f));
    }
    
    public void addScopedSearchWithFacetUnselected(SearchTransaction st, FacetDefinition facet) {
        SearchQuestion extraQuestion = extraSearchFacetsSelectedAndCountsPossible(st);
        
        // We need to disable the current facet.
        Map<String, List<String>> rawAsList = MapUtils.convertMapList(extraQuestion.getRawInputParameters());
        
        new FillFacetUrls().unselectFacet(rawAsList, facet);
        
        extraQuestion.getRawInputParameters().clear();
        
        extraQuestion.getRawInputParameters().putAll(MapUtils.convertMapArray(rawAsList));
        
        String extraSearchName = new FacetExtraSearchNames().extraSearchWithFacetUnchecked(facet);
        st.addExtraSearch(extraSearchName, extraQuestion);
    }
    
    public void addDedicatedExtraSearchesForOrFacetCounts(SearchTransaction searchTransaction) {        
        // Facets that can expand the result set must use a extra search to find counts.
        Map<String, FacetDefinition> facetsThatNeedASearchForCounts = getFacetDefinitions(searchTransaction)
        .stream()
        .filter(f -> facetedNavProps.useDedicatedExtraSearchForCounts(f, searchTransaction))
        // remove facets that are 
        .collect(Collectors.toMap(f -> f.getName(), f -> f));
        
        
        
        if(facetsThatNeedASearchForCounts.isEmpty()) {
            log.debug("No need to run indvidual extra searches for OR counts either because no OR type facets have been selected.");
            return;
        }
        
        AtomicInteger extraSearchesAdded = new AtomicInteger(0);
        int maxExtraSearchesThatCanBeAdded = searchTransaction.getQuestion().getCurrentProfileConfig()
                .get(com.funnelback.config.keys.Keys.FrontEndKeys.ModernUI.MAX_FACET_EXTRA_SEARCHES);
        
        
        // Go over each facet that needs to run the search to work out the count
        // find its value supplying extra search name then submit the extra searches.
        for(FacetDefinition facetDefinition : facetsThatNeedASearchForCounts.values()) {
            createSearchesWhereTheTotalMatchingIsTheCountOfTheFacet(searchTransaction, 
                facetDefinition,
                valueProvidingSearchName(facetDefinition), 
                extraSearchesAdded, 
                maxExtraSearchesThatCanBeAdded);
        }
        
        log.debug("Added {} extra searches", extraSearchesAdded);
        
    }
    
    /**
     * Return the name of the extra search that should be used to work out the queries to run
     * for OR and other facets that need to run the actual search to work out count.
     * 
     * @param facetWhichUsesActualSearchForCounts 
     * @return
     */
    public String valueProvidingSearchName(FacetDefinition facetWhichUsesActualSearchForCounts) {
        if(facetWhichUsesActualSearchForCounts.getFacetValues() == FROM_UNSCOPED_QUERY) {
            return SEARCH_FOR_UNSCOPED_VALUES;
        }
        
        if(facetWhichUsesActualSearchForCounts.getFacetValues() == FROM_UNSCOPED_ALL_QUERY) {
            return SEARCH_FOR_ALL_VALUES;
        }
        
        if(facetWhichUsesActualSearchForCounts.getFacetValues() == FROM_SCOPED_QUERY_WITH_FACET_UNSELECTED) {
            return facetExtraSearchNames.extraSearchWithFacetUnchecked(facetWhichUsesActualSearchForCounts);
        }
        
        throw new IllegalStateException("Facet " 
                                    + facetWhichUsesActualSearchForCounts.getName() 
                                    + " is not one that needs to run the actual search to get a count.");
        
    }
    
    /**
     * In cases like multiple OR we have to run the actual search that would be run
     * if the facet was clicked to work out the counts.
     * 
     * This takes care creating those searches to be run.
     * 
     * @param searchTransaction
     * @param facetDefinition The facetDefinition that needs to run the actual search to get the count.
     * @param extraSearchProvidingValues This is the name of the already submitted extra search that provides
     * the values that will be displayed for the facet and thus tells us what queries we need to run. 
     * @param extraSearchesAdded Holds a count of the number of extra searches that can be run.
     * @param maxExtraSearchesThatCanBeAdded the count of extra searches which can be run.
     */
    public void createSearchesWhereTheTotalMatchingIsTheCountOfTheFacet(SearchTransaction searchTransaction, 
        FacetDefinition facetDefinition,
        String extraSearchProvidingValues,
        AtomicInteger extraSearchesAdded,
        int maxExtraSearchesThatCanBeAdded) {
        
        
        Optional<SearchTransaction> valueProvidingSearch = this.extraSearchesExecutor
            .getAndMaybeWaitForExtraSearch(searchTransaction, extraSearchProvidingValues);
        
        if(!valueProvidingSearch.isPresent()) {
            log.warn("Can not calculate counts for facet {} as the extra search '{}' is missing",
                facetDefinition.getName(),
                extraSearchProvidingValues);
            return;
        }
        
        List<OptionAndValue> extraPadreOptions = padreOptionsForSpeed.getOptionsThatDoNotAffectResultSetAsPairs();
        for(Facet facet : valueProvidingSearch.map(SearchTransaction::getResponse).map(SearchResponse::getFacets).orElse(Collections.emptyList())) {
            if(facetDefinition.getName().equals(facet.getName())) {
                for(CategoryValue value : Optional.ofNullable(facet.getAllValues()).orElse(Collections.emptyList())) {
                    
                    if(extraSearchesAdded.incrementAndGet() > maxExtraSearchesThatCanBeAdded) {
                        log.info("Limit of extra searches has been reached try increasing '{}'", 
                            com.funnelback.config.keys.Keys.FrontEndKeys.ModernUI.MAX_FACET_EXTRA_SEARCHES.getKey());
                        searchTransaction.setAnyExtraSearchesIncomplete(true);
                        return;
                    }
                    
                    String extraSearchName = new FacetExtraSearchNames().extraSearchToCalculateCounOfCategoryValue(facet, value);
                    SearchQuestion extraQuestion = new FacetedNavigationQuestionFactory().buildBasicExtraFacetSearch(searchTransaction.getQuestion());
                    
                    // Update the query as though we had selected the facet value.
                    extraQuestion.getRawInputParameters().keySet().stream().collect(Collectors.toList())
                        .forEach(extraQuestion.getRawInputParameters()::remove);
             
                    
                    QueryStringUtils.toMap(value.getSelectUrl())
                        .forEach((k, v) -> extraQuestion.getRawInputParameters().put(k, v.toArray(new String[0])));
                    
                    // For this query we are interested only in the total matching so lets
                    // not count rmcf or anything else, we need to speed these extra searches up
                    // as we have many of these.
                    
                    addFacetExtraSearchSpecificPadreOptions(extraQuestion, extraPadreOptions);
                    
                    searchTransaction.addExtraSearch(extraSearchName, extraQuestion);
                    log.trace("Added extra search '{}' for facet {} and category value data: {}", 
                        extraSearchName,
                        facet.getName(),
                        value.getData());
                }
            }
        }
    }
    
    public void addFacetExtraSearchSpecificPadreOptions(SearchQuestion searchQuestion, List<OptionAndValue> optionsAndValues) {
        searchQuestion.getPriorityQueryProcessorOptions().addOption(QueryProcessorOptionKeys.LOG, "false");
        searchQuestion.getPriorityQueryProcessorOptions().addOption(QueryProcessorOptionKeys.NUM_RANKS, "0");
        optionsAndValues
            .stream()
            .forEach(p -> {
                searchQuestion.getPriorityQueryProcessorOptions().addOption(p.getOption(), p.getValue());
            });
        
    }
    
    
    
    public List<FacetDefinition> getFacetDefinitions(SearchTransaction st) {
        return Optional.ofNullable(FacetedNavigationUtils.selectConfiguration(st))
            .map(c -> c.getFacetDefinitions())
            .orElse(Collections.emptyList());
    }
    
    public boolean doAnyFacetsNeedUnscopedQuery(SearchTransaction st) {
         return getFacetDefinitions(st)
             .stream()
             .anyMatch(f -> f.getFacetValues() == FacetValues.FROM_UNSCOPED_QUERY || // Anything which uses the unscoped query to find its values.
                     new FacetedNavigationProperties().useUnscopedQueryForCounts(f, st));
    }
    
    public boolean doAnyFacetsNeedAllQuery(SearchTransaction st) {
        return getFacetDefinitions(st)
            .stream()
            .filter(f -> f.getFacetValues() == FacetValues.FROM_UNSCOPED_ALL_QUERY)
            .map(FacetDefinition::getCategoryDefinitions)
            .flatMap(i -> i.stream())
            .flatMap(Flattener.mapper(CategoryDefinition::getSubCategories))
            .anyMatch(not(CategoryDefinition::allValuesDefinedByUser));
   }
    
    

}
