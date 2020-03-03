package com.funnelback.publicui.search.model.transaction.facet.order;

import java.util.Comparator;

import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;

public class FacetComparators {

    public static final Comparator<CategoryValue> BY_COUNT_ASCENDING =
        Comparator.comparing(CategoryValue::getCount, Comparator.nullsLast(Comparator.naturalOrder()));
    
    public static final Comparator<CategoryValue> BY_COUNT_DESCENDING = 
        Comparator.comparing(CategoryValue::getCount, Comparator.nullsLast(Comparator.reverseOrder()));
    
    public static final Comparator<CategoryValue> BY_LABEL_ASCENDING =
        Comparator.comparing(CategoryValue::getLabel, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
    
    public static final Comparator<CategoryValue> BY_LABEL_DESCENDING =
        Comparator.comparing(CategoryValue::getLabel, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER.reversed()));
    
    public static final Comparator<CategoryValue> BY_LABEL_AS_NUMBER_ASCENDING =
        Comparator.comparing(CategoryValue::getLabel, Comparator.nullsLast(new ByStringAsNumberComparator()));
    
    public static final Comparator<CategoryValue> BY_LABEL_AS_NUMBER_DESCENDING =
        Comparator.comparing(CategoryValue::getLabel, Comparator.nullsLast(new ByStringAsNumberComparator().reversed()));
    
}
