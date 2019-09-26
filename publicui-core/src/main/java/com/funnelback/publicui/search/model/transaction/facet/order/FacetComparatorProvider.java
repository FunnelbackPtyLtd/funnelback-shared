package com.funnelback.publicui.search.model.transaction.facet.order;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.ComparatorUtils;

import com.funnelback.common.config.DefaultValues.FacetedNavigation.DateSortMode;
import com.funnelback.common.facetednavigation.models.FacetValuesOrder;
import com.funnelback.common.function.TakeWhile;
import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.google.common.collect.ImmutableMap;

public class FacetComparatorProvider {

    private final Map<FacetValuesOrder, Comparator<Facet.CategoryValue>> FACET_ORDER_TO_COMPARATOR 
        = ImmutableMap.<FacetValuesOrder, Comparator<Facet.CategoryValue>>builder()
        .put(FacetValuesOrder.COUNT_DESCENDING, FacetComparators.BY_COUNT_DESCENDING)
        .put(FacetValuesOrder.COUNT_ASCENDING, FacetComparators.BY_COUNT_ASCENDING)
        .put(FacetValuesOrder.SELECTED_FIRST, new BySelectedFirst())
        .put(FacetValuesOrder.CATEGORY_DEFINITION_ORDER, new FacetConfigOrder())
        .put(FacetValuesOrder.LABEL_ASCENDING, FacetComparators.BY_LABEL_ASCENDING)
        .put(FacetValuesOrder.LABEL_DESCENDING, FacetComparators.BY_LABEL_DESCENDING)
        .put(FacetValuesOrder.LABEL_AS_NUMBER_ASCENDING, FacetComparators.BY_LABEL_AS_NUMBER_ASCENDING)
        .put(FacetValuesOrder.LABEL_AS_NUMBER_DESCENDING, FacetComparators.BY_LABEL_AS_NUMBER_DESCENDING)
        .put(FacetValuesOrder.DATE_ASCENDING, new ByDateComparator(DateSortMode.adate))
        .put(FacetValuesOrder.DATE_DESCENDING, new ByDateComparator(DateSortMode.ddate))
        .build();
    
    Comparator<Facet.CategoryValue> getComparator(FacetValuesOrder orderToSortBy, Optional<Comparator<Facet.CategoryValue>> customComparator) {
        if(orderToSortBy == FacetValuesOrder.CUSTOM_COMPARATOR) {
            return customComparator.orElse(new AsIsComparator());
        }
        return FACET_ORDER_TO_COMPARATOR.get(orderToSortBy);
    }
    
    /**
     * Creates a comparator by chaining the wanted comparators.
     * 
     * @param ordersToSortBy The list describing the comparators wanted.
     * @return
     */
    Comparator<Facet.CategoryValue> makeComparatorChain(List<FacetValuesOrder> ordersToSortBy,
                                                        Optional<Comparator<Facet.CategoryValue>> customComparator) {
        if(ordersToSortBy == null || ordersToSortBy.isEmpty()) {
            return new AsIsComparator();
        }
        return ComparatorUtils.chainedComparator(ordersToSortBy.stream().map(o -> getComparator(o, customComparator)).collect(Collectors.toList()));
    }
    
    /**
     * This is the comparator to use when we are sorting values that come from a Single CategoryDefinition.
     * 
     * <p>This one needs to ensure that even caparators that come after
     * CATEGORY_DEFINITION_ORDER are used as we are comparing within
     * the category definition.</p>
     * @param ordersToSortBy
     * @return
     */
    public Comparator<Facet.CategoryValue> getComparatorWhenSortingValuesFromSingleCategory(List<FacetValuesOrder> ordersToSortBy) {
        return makeComparatorChain(ordersToSortBy, Optional.empty());
    }
    
    /**
     * Gets the comparator to use when we are sorting on ALL category values.
     * 
     * This comparator is expected to be able to sort values from all sources
     * (ie all category definitions). In this case we need to be careful 
     * to ensure that sorting by CATEGORY_DEFINITION_ORDER still works.
     * 
     * @param ordersToSortBy
     * @return
     */
    public Comparator<Facet.CategoryValue> getComparatorWhenSortingAllValus(List<FacetValuesOrder> ordersToSortBy, 
            Optional<Comparator<Facet.CategoryValue>> customComparator) {
        // As we are sorting all values don't use any comparator that comes after
        // sorting by CATEGORY_DEFINITION_ORDER as that sort is already done
        // (as we sort the values returned by the category definition). We need to
        // do this as the comparator CATEGORY_DEFINITION_ORDER allways returns zero
        // as it wants to preserve the ordering, that will result in the following
        // comparators being used, so we remove them.
        return makeComparatorChain(TakeWhile
                            .takeWhile(o -> o != FacetValuesOrder.CATEGORY_DEFINITION_ORDER, ordersToSortBy.stream())
                            .collect(Collectors.toList()), customComparator);
    }
    
    private static class AsIsComparator implements Comparator<Facet.CategoryValue> {

        @Override
        public int compare(CategoryValue o1, CategoryValue o2) {
            return 0;
        }
        
    }
}
