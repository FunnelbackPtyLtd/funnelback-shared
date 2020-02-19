package com.funnelback.publicui.search.model.transaction.facet.order;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

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
    
    private final Comparator<Integer> comparator;

    public ByDateComparator(boolean ascending) {
        Comparator<Integer> comparator = Comparator.naturalOrder();
        if(!ascending) {
            comparator = comparator.reversed();
        }
        
        this.comparator = Comparator.nullsLast(comparator);
    }
    
    @Override
    public int compare(CategoryValue cv1, CategoryValue cv2) {
        return this.comparator.compare(getIntegerFromCategory(cv1), getIntegerFromCategory(cv2));
    }
    
    public Integer getIntegerFromCategory(CategoryValue catVal) {
        if (DATE_LABELS_VALUES.containsKey(catVal.getLabel())) {
            return DATE_LABELS_VALUES.get(catVal.getLabel());
        } else {
            return tryParseInt(catVal.getLabel());
        }
    }
    
    public Integer tryParseInt(String val) {
        try {
            return Integer.parseInt(val);
        } catch (Exception e) {
            return null;
        }
    }
}