package com.funnelback.publicui.search.lifecycle.output.processors;

import java.util.Collections;

import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.DefaultValues.FacetedNavigation.DateSortMode;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.lifecycle.output.AbstractOutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.DateFieldFill;
import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.Facet.Category;
import com.funnelback.publicui.search.model.transaction.facet.order.ByDateComparator;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.utils.FacetedNavigationUtils;

/**
 * Sort date based facets by chronological order
 * instead of the default order (by count)
 * 
 * @since v12.5
 */
@Log4j2
@Component("dateFacetSortOutputProcessor")
public class DateFacetSort extends AbstractOutputProcessor {

    @Override
    public void processOutput(SearchTransaction searchTransaction) throws OutputProcessorException {
        if (SearchTransactionUtils.hasCollection(searchTransaction)
            && SearchTransactionUtils.hasResponse(searchTransaction)
            && searchTransaction.getResponse().hasResultPacket()) {
            
            String sortMode = searchTransaction.getQuestion().getCollection().getConfiguration().value(
                Keys.FacetedNavigation.DATE_SORT_MODE,
                DefaultValues.FacetedNavigation.DateSortMode.count.toString());
            
            if (DefaultValues.FacetedNavigation.DateSortMode.adate.toString().equals(sortMode)
                || DefaultValues.FacetedNavigation.DateSortMode.ddate.toString().equals(sortMode)) {

                FacetedNavigationConfig config = FacetedNavigationUtils.selectConfiguration(
                    searchTransaction.getQuestion().getCollection(),
                    searchTransaction.getQuestion().getProfile());
                
                if (config == null) {
                    return;
                }
                
                final ByDateComparator comparator = new ByDateComparator(DateSortMode.valueOf(sortMode));
                
                // Each facet
                for(FacetDefinition facetDef: config.getFacetDefinitions()) {
                    // Each category, within each facet
                    for (CategoryDefinition catDef: facetDef.getCategoryDefinitions()) {
                        if (catDef instanceof DateFieldFill) {
                            // Date based category, find corresponding values
                            // in the response
                            for (Facet facet: searchTransaction.getResponse().getFacets()) {
                                if (facet.getName().equals(facetDef.getName())) {
                                    for (Category cat: facet.getCategories()) {
                                        if (cat.getQueryStringParamName().equals(catDef.getQueryStringParamName())) {
                                            log.debug("Sorting category '"+cat.getQueryStringParamName()
                                                +"' for facet '"+facet.getName()+"'");
                                            sortCategory(cat, comparator);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Recursively sort a category
     * @param category {@link Category} to sort
     * @param comparator Comparator to use for sorting
     */
    private void sortCategory(Category category, ByDateComparator comparator) {
        Collections.sort(category.getValues(), comparator);
        // Recursively sort sub-categories
        for (Category c: category.getCategories()) {
            sortCategory(c, comparator);
        }
    }

}
