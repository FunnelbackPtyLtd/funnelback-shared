package com.funnelback.publicui.search.model.transaction.facet.order;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.funnelback.common.facetednavigation.models.FacetValuesOrder;
import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.google.common.collect.ImmutableMap;

import lombok.AllArgsConstructor;

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
        .put(FacetValuesOrder.DATE_ASCENDING, new ByDateComparator(true))
        .put(FacetValuesOrder.DATE_DESCENDING, new ByDateComparator(false))
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
        
        List<Comparator<Facet.CategoryValue>> comparators = ordersToSortBy.stream().map(o -> getComparator(o, customComparator)).collect(Collectors.toList());
        return new ChainComparator<>(comparators);
    }
    
    @AllArgsConstructor
    private static class ChainComparator<T> implements Comparator<T> {
        private List<Comparator<T>> comparatorList;   
        
        @Override
        public int compare(T o1, T o2) {
            for(Comparator<T> comparator : comparatorList) {
                int res = comparator.compare(o1, o2);
                if(res != 0) return res;
            }
            return 0;
        }
        
    }
    
    /**
     * Gets the comparator to use when we are sorting on ALL category values.
     * 
     * 
     * @param ordersToSortBy
     * @return
     */
    public Comparator<Facet.CategoryValue> getComparatorWhenSortingAllValus(List<FacetValuesOrder> ordersToSortBy, 
            Optional<Comparator<Facet.CategoryValue>> customComparator) {
        return makeComparatorChain(ordersToSortBy, customComparator);
    }
    
    private static class AsIsComparator implements Comparator<Facet.CategoryValue> {

        @Override
        public int compare(CategoryValue o1, CategoryValue o2) {
            return 0;
        }
        
    }
}
