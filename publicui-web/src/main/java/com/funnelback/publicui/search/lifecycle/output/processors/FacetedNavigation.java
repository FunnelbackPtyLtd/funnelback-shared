package com.funnelback.publicui.search.lifecycle.output.processors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.log4j.Log4j2;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.output.AbstractOutputProcessor;
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.Facet.Category;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.utils.FacetedNavigationUtils;
import com.google.common.annotations.VisibleForTesting;

/**
 * Transforms result metadata/url/gscope counts into proper facet hierarchy
 */
@Component("facetedNavigationOutputProcessor")
@Log4j2
public class FacetedNavigation extends AbstractOutputProcessor {

    @Override
    public void processOutput(SearchTransaction searchTransaction) {
        if (SearchTransactionUtils.hasCollection(searchTransaction)
                && SearchTransactionUtils.hasResponse(searchTransaction)
                && searchTransaction.getResponse().hasResultPacket()) {
            
            FacetedNavigationConfig config = FacetedNavigationUtils.selectConfiguration(searchTransaction.getQuestion().getCollection(), searchTransaction.getQuestion().getProfile());
            
            if (config != null) {
                List<String> sortOrder = Arrays.asList(new String[]{}); // TODO - Decide how to populate this order of names
                List<FacetDefinition> sortedFacets = sortFacetDefinitions(config.getFacetDefinitions(), sortOrder);
                
                for(FacetDefinition f : sortedFacets) {
                    final Facet facet = new Facet(f.getName());
                    
                    for (final CategoryDefinition ct: f.getCategoryDefinitions()) {
                        Facet.Category c = fillCategories(ct, searchTransaction);
                        if (c != null) {
                            facet.getCategories().add(c);
                            if (searchTransaction.getQuestion().getSelectedCategoryValues().containsKey(ct.getQueryStringParamName())) {
                                // This category has been selected. Stop calculating other
                                // values for it (FUN-4462)
                                break;
                            }
                        }
                    }
                    
                    // Sort all categories for this facet by the count of the first value of
                    // each category. This is useful for GScope based facets where there's only
                    // one category-value per category
                    Collections.sort(facet.getCategories(), new Category.ByFirstCategoryValueComparator());
                    searchTransaction.getResponse().getFacets().add(facet);
                }
            }
        }
    }

    /**
     * @return A new list with the provided FacetDefinitions sorted based on the facetNameOrder (and the names of the facets)
     */
    @VisibleForTesting
    public List<FacetDefinition> sortFacetDefinitions(List<FacetDefinition> facetDefinitions, List<String> facetNameOrder) {
        List<FacetDefinition> sortedFacets = new ArrayList<FacetDefinition>(facetDefinitions);

        Map<String, Integer> nameToFacetRank = new HashMap<>();
        for (int i = 0; i < facetNameOrder.size(); i++) {
            nameToFacetRank.put(facetNameOrder.get(i), i);
        }

        sortedFacets.sort((a, b) -> {
            return Integer.compare(
                (nameToFacetRank.containsKey(a.getName()) ? nameToFacetRank.get(a.getName()) : Integer.MAX_VALUE),
                (nameToFacetRank.containsKey(b.getName()) ? nameToFacetRank.get(b.getName()) : Integer.MAX_VALUE));
        });
        
        return sortedFacets;
    }

    /**
     * Fills the categories for a given {@link CategoryDefinition}, by computing all the
     * values from the result packet (Delegated to the {@link CategoryDefinition} implementation.
     * 
     * If this category has been selected by the user, fills the sub-categories.
     * 
     * @param ct
     * @param searchTransaction
     * @return
     */
    private Facet.Category fillCategories(final CategoryDefinition cDef, SearchTransaction searchTransaction) {
        log.trace("Filling category '" + cDef + "'");
        
        // Fill the values for this category
        Category category = new Category(cDef.getLabel(), cDef.getQueryStringParamName());
        category.getValues().addAll(cDef.computeValues(searchTransaction));
        Collections.sort(category.getValues(), new CategoryValue.ByCountComparator(true));
    
        // Find out if this category is currently selected
        if (searchTransaction.getQuestion().getSelectedCategoryValues().containsKey(cDef.getQueryStringParamName())) {
            
            // Something has been selected (f.FacetName|extraparm=value)
            String[] paramName = cDef.getQueryStringParamName().split("\\" + CategoryDefinition.QS_PARAM_SEPARATOR);
            
            // Find out the extra param (Usually a MD letter, or a gscope number
            final String extraParam;
            if (paramName.length > 1) {
                extraParam = paramName[1];
            } else {
                extraParam = null;
            }
            log.trace("Category has been selected. Extra param is '" + extraParam + "'");
            
            // Find out if a value for this category matches the selection
            List<String> selectedValues = searchTransaction.getQuestion().getSelectedCategoryValues().get(cDef.getQueryStringParamName());
            boolean valueMatches = CollectionUtils.exists(selectedValues, new Predicate() {
                @Override
                public boolean evaluate(Object o) {
                    return cDef.matches((String) o, extraParam);
                }
            });
        
            if (valueMatches) {
                log.trace("Value has been selected.");
                for (CategoryDefinition subCategoryDef: cDef.getSubCategories()) {
                    Facet.Category c = fillCategories(subCategoryDef, searchTransaction);
                    if (c != null) {
                        category.getCategories().add(c);
                        if (searchTransaction.getQuestion().getSelectedCategoryValues().containsKey(subCategoryDef.getQueryStringParamName())) {
                            // This category has been selected. Stop calculating other
                            // values for it (FUN-4462)
                            break;
                        }
                    }
                }
            }
        }
        
        if (category.hasValues() || category.getCategories().size() > 0) {
            return category;
        } else {
            // No value and no nested category. This can happen for gscope facets.
            // No need to return it.
            return null;
        }
    }

}
