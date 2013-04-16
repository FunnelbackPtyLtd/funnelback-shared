package com.funnelback.publicui.search.lifecycle.output.processors;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j;

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
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.utils.FacetedNavigationUtils;

/**
 * Sort date based facets by chronological order
 * instead of the default order (by count)
 * 
 * @since v12.5
 */
@Log4j
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
    
    /**
     * Compare {@link CategoryValue} containing date-based facet values
     * and sort them in reverse chronological order.
     */
    private static class ByDateComparator implements Comparator<CategoryValue> {

        private static final Map<String, Integer> DATE_LABELS_VALUES = new HashMap<>();
        // Assign high values to special labels so that they get
        // sorted before the year facets
        static {
            DATE_LABELS_VALUES.put("Coming year", 900012);
            DATE_LABELS_VALUES.put("Coming month", 900011);
            DATE_LABELS_VALUES.put("Coming week", 900010);
            DATE_LABELS_VALUES.put("Tomorrow", 900009);
            DATE_LABELS_VALUES.put("Today", 900008);
            DATE_LABELS_VALUES.put("Yesterday", 900007);
            DATE_LABELS_VALUES.put("Past week", 900006);
            DATE_LABELS_VALUES.put("Past fortnight", 900005);
            DATE_LABELS_VALUES.put("Past month", 900004);
            DATE_LABELS_VALUES.put("Past 3 months", 900003);
            DATE_LABELS_VALUES.put("Past 6 months", 900002);
            DATE_LABELS_VALUES.put("Past year", 900001);
            DATE_LABELS_VALUES.put("Uncertain", 900000);
        }
        
        private final DateSortMode sortMode;

        public ByDateComparator(DateSortMode sortMode) {
            if (! DateSortMode.adate.equals(sortMode)
                && ! DateSortMode.ddate.equals(sortMode)) {
                throw new IllegalArgumentException("Unsupported sort mode " + sortMode);
            }
            this.sortMode = sortMode;
        }
        
        @Override
        public int compare(CategoryValue cv1, CategoryValue cv2) {
            int value1 = 0;
            int value2 = 0;
            
            if (DATE_LABELS_VALUES.containsKey(cv1.getLabel())) {
                value1 = DATE_LABELS_VALUES.get(cv1.getLabel());
            } else {
                value1 = Integer.parseInt(cv1.getLabel());
            }
            
            if (DATE_LABELS_VALUES.containsKey(cv2.getLabel())) {
                value2 = DATE_LABELS_VALUES.get(cv2.getLabel());
            } else {
                value2 = Integer.parseInt(cv2.getLabel());
            }
        
            if (DateSortMode.ddate.equals(sortMode)) {
                return value2 - value1;
            } else {
                return value1 - value2;
            }
            
        }
    }

}
