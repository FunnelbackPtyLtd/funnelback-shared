package com.funnelback.publicui.search.model.transaction.facet.order;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import com.funnelback.common.config.DefaultValues.FacetedNavigation.DateSortMode;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;

/**
 * Compare {@link CategoryValue} containing date-based facet values
 * and sort them in reverse chronological order.
 */
public class ByDateComparator implements Comparator<CategoryValue> {

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